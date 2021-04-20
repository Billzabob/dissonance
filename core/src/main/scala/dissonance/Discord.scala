package dissonance

import java.io.IOException

import cats.Applicative
import cats.effect._
import cats.effect.concurrent._
import cats.syntax.all._
import dissonance.Discord._
import dissonance.data.ControlMessage._
import dissonance.data._
import dissonance.data.events.Ready
import dissonance.utils._
import fs2.Stream
import fs2.concurrent.Queue
import io.circe.Json
import io.circe.parser._
import io.circe.syntax._
import org.http4s.Method._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.client.Client
import org.http4s.client.jdkhttpclient.WSFrame._
import org.http4s.client.jdkhttpclient._
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.{headers => _, _}

import scala.concurrent.duration._
import cats.effect.{ Deferred, Ref, Temporal }

class Discord[F[_]: Concurrent](token: String, val httpClient: Client[F], wsClient: WSClient[F])(implicit cs: ContextShift[F], t: Temporal[F]) {
  type SequenceNumber    = Ref[F, Option[Int]]
  type SessionId         = Ref[F, Option[String]]
  type Acks              = Queue[F, Unit]
  type HeartbeatInterval = Deferred[F, FiniteDuration]
  case class DiscordState(sequenceNumber: SequenceNumber, sessionId: SessionId, acks: Acks)

  val client = new DiscordClient(token, httpClient)

  def addMiddleware(middleware: Client[F] => Client[F]): Discord[F] =
    new Discord(token, middleware(httpClient), wsClient)

  def subscribe(shard: Shard, intents: Intent*): Stream[F, Event] = subscribe(shard, intents.toList)

  def subscribe(shard: Shard, intents: List[Intent]): Stream[F, Event] = {
    val sequenceNumber = Ref[F].of(none[Int])
    val sessionId      = Ref[F].of(none[String])
    val acks           = Queue.unbounded[F, Unit]

    val events = for {
      state <- (sequenceNumber, sessionId, acks).mapN(DiscordState)
      uri   <- getUri
    } yield processEvents(uri, shard, intents, state)

    Stream.force(events)
  }

  private def getUri: F[Uri] =
    httpClient
      .expect[GetGatewayResponse](
        Request[F]()
          .withMethod(GET)
          .withUri(apiEndpoint.addPath("gateway/bot"))
          .withHeaders(headers(token))
      ) // TODO: Add log if gateway recommends more shards
      .map(_.url)
      .map(Uri.fromString)
      .rethrow
      .map(_.withQueryParam("v", 8).withQueryParam("encoding", "json"))

  private def processEvents(uri: Uri, shard: Shard, intents: List[Intent], state: DiscordState): Stream[F, Event] =
    (connection(uri) zip heartbeatInterval)
      .flatMap { case (connection, interval) =>
        events(connection, shard, intents, state, interval)
      }
      .recoverWith { case _: IOException =>
        Stream.empty // We can get all sorts of random IO errors, all we can do is restart the connection
      }
      .repeat

  private def events(connection: WSConnectionHighLevel[F], shard: Shard, intents: List[Intent], state: DiscordState, interval: HeartbeatInterval): Stream[F, Event] = {
    connection.receiveStream
      .collect {
        // Will always be text since we request JSON encoding
        case Text(data, _) => data
      }
      .map(decode[ControlMessage])
      .rethrow
      .parEvalMap(Int.MaxValue)(controlMessage => handleEvents(controlMessage, connection, shard, intents, state, interval))
      .takeWhile(result => !result.terminate)
      .collect { case Result(Some(event)) => event }
      .concurrently(heartbeat(connection, interval, state.sequenceNumber, state.acks))
  }

  private def handleEvents(
      controlMessage: ControlMessage,
      connection: WSConnectionHighLevel[F],
      shard: Shard,
      intents: List[Intent],
      state: DiscordState,
      interval: HeartbeatInterval
  ): F[EventResult] =
    controlMessage match {
      case Hello(intervalDuration) =>
        interval.complete(intervalDuration) >> identifyOrResume(state.sessionId, state.sequenceNumber, shard, intents).flatMap(connection.send).as(Result(None))
      case HeartBeatAck =>
        state.acks.enqueue1(()).as(Result(None))
      case Heartbeat(d) =>
        putStrLn(s"Heartbeat received: $d").as(Result(None))
      case Reconnect =>
        Applicative[F].pure(Terminate)
      case InvalidSession(resumable) =>
        state.sessionId.set(None).whenA(!resumable) >> t.sleep(5.seconds).as(Terminate)
      case Dispatch(nextSequenceNumber, event) =>
        setSessionId(event, state.sessionId) >> state.sequenceNumber.set(nextSequenceNumber.some).as(Result(event.some))
    }

  private def identifyOrResume(sessionId: SessionId, sequenceNumber: SequenceNumber, shard: Shard, intents: List[Intent]): F[Text] =
    sessionId.get.flatMap {
      case None     => identifyMessage(shard, intents).pure
      case Some(id) => sequenceNumber.get.map(s => resumeMessage(id, s))
    }

  private def connection(uri: Uri): Stream[F, WSConnectionHighLevel[F]] =
    Stream.resource(wsClient.connectHighLevel(WSRequest(uri, Headers.of(headers(token)))))

  private def heartbeatInterval: Stream[F, HeartbeatInterval] =
    Stream.eval(Deferred[F, FiniteDuration])

  private def setSessionId(event: Event, sessionId: SessionId): F[Unit] =
    event match {
      case Ready(_, _, id, _) => sessionId.set(id.some)
      case _                  => Applicative[F].unit
    }

  private def heartbeat(connection: WSConnectionHighLevel[F], interval: HeartbeatInterval, sequenceNumber: SequenceNumber, acks: Acks): Stream[F, Unit] =
    Stream.eval(interval.get).flatMap { interval =>
      val sendHeartbeat = makeHeartbeat(sequenceNumber).flatMap(connection.send)
      val heartbeats    = Stream.eval(sendHeartbeat) ++ Stream.repeatEval(sendHeartbeat).metered(interval)

      // TODO: Something besides true, false
      (heartbeats.as(true) merge acks.dequeue.as(false)).zipWithPrevious.flatMap {
        case (Some(true), true) => Stream.raiseError[F](Errors.NoHeartbeatAck)
        case _                  => Stream.emit(())
      }
    }

  private def makeHeartbeat(sequenceNumber: SequenceNumber) =
    sequenceNumber.get.map(Heartbeat.apply).map(heartbeat => Text(heartbeat.asJson.noSpaces))

  private def identifyMessage(shard: Shard, intents: List[Intent]) = // TODO: Make a case class for the op, d structure that is also used in ControlMessage
    Text(
      Json
        .obj(
          "op" -> 2.asJson,
          "d"  -> Identify(token, IdentifyConnectionProperties("", "", ""), None, None, shard.some, None, None, intents).asJson
        )
        .noSpaces
    )

  private def resumeMessage(sessionId: String, sequenceNumber: Option[Int]) =
    Text(s"""{"op":6,"d":{"token":"$token","session_id":"$sessionId","seq":"$sequenceNumber"}}""")
}

object Discord {
  def make[F[_]: ConcurrentEffect](token: String)(implicit t: Temporal[F]): Resource[F, Discord[F]] =
    Resource.eval(utils.javaClient.map(javaClient => new Discord(token, JdkHttpClient[F](javaClient), JdkWSClient[F](javaClient))))

  val apiEndpoint                           = uri"https://discordapp.com/api/v8"
  def headers(token: String): Authorization = Authorization(Credentials.Token("Bot".ci, token))

  sealed trait EventResult                extends Product with Serializable { val terminate: Boolean }
  case class Result(event: Option[Event]) extends EventResult               { val terminate = false  }
  case object Terminate                   extends EventResult               { val terminate = true   }
}
