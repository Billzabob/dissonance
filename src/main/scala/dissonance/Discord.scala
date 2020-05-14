package dissonance

import cats.effect._
import cats.effect.concurrent._
import cats.implicits._
import dissonance.Discord._
import dissonance.model._
import dissonance.model.ControlMessage._
import dissonance.model.Errors._
import dissonance.model.Event._
import dissonance.model.gateway._
import dissonance.model.identify.{Identify, IdentifyConnectionProperties}
import dissonance.model.intents.Intent
import dissonance.utils._
import fs2.concurrent.Queue
import fs2.Stream
import io.circe.Json
import io.circe.parser._
import io.circe.syntax._
import org.http4s.{headers => _, _}
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.client.Client
import org.http4s.client.dsl.io._
import org.http4s.client.jdkhttpclient._
import org.http4s.client.jdkhttpclient.WSFrame._
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.Method._

import scala.concurrent.duration._

class Discord(token: String, httpClient: Client[IO], wsClient: WSClient[IO])(implicit cs: ContextShift[IO], t: Timer[IO]) {

  val client = new DiscordClient(token, httpClient)

  def subscribe(intents: Intent*): Stream[IO, Event] = subscribe(intents.toList)

  def subscribe(intents: List[Intent]): Stream[IO, Event] = {
    val sequenceNumber = Ref[IO].of(none[Int])
    val sessionId      = Ref[IO].of(none[String])
    val acks           = Queue.unbounded[IO, Unit]

    val events = for {
      state <- (sequenceNumber, sessionId, acks).mapN(DiscordState)
      uri   <- getUri
    } yield processEvents(uri, intents, state)

    Stream.force(events)
  }

  private def getUri: IO[Uri] =
    httpClient
      .expect[GetGatewayResponse](GET(apiEndpoint.addPath("gateway/bot"), headers(token)))
      .map(_.url)
      .map(Uri.fromString)
      .rethrow
      .map(_.withQueryParam("v", 6).withQueryParam("encoding", "json"))

  private def processEvents(uri: Uri, intents: List[Intent], state: DiscordState): Stream[IO, Event] =
    Stream
      .resource(wsClient.connectHighLevel(WSRequest(uri, Headers.of(headers(token)))))
      .flatMap(connection => events(connection, intents, state))
      .repeat

  private def events(connection: WSConnectionHighLevel[IO], intents: List[Intent], state: DiscordState): Stream[IO, Event] = {
    connection.receiveStream
      .collect {
        // Will always be text since we request JSON encoding
        case Text(data, _) => data
      }
      .map(decode[ControlMessage])
      .rethrow
      .map(event => handleEvents(event, connection, intents, state))
      // TODO: If we receive `Some(event)` and `None` concurrently, we could potentially drop `event` here
      .parJoinUnbounded
      .unNoneTerminate
      .interruptWhen(connection.closeFrame.get.map(checkForGracefulClose))
  }

  // TODO: This could probably be simplified if we just return IO instead of Stream
  private def handleEvents(event: ControlMessage, connection: WSConnectionHighLevel[IO], intents: List[Intent], state: DiscordState): Stream[IO, Option[Event]] =
    event match {
      case Hello(interval) =>
        Stream.eval_(identifyOrResume(state.sessionId, state.sequenceNumber, intents).flatMap(connection.send)) ++ heartbeat(
          interval,
          connection,
          state.sequenceNumber,
          state.acks
        ).drain
      case HeartBeatAck =>
        Stream.eval_(state.acks.enqueue1(()))
      case Heartbeat(d) =>
        Stream.eval_(putStrLn(s"Heartbeat received: $d"))
      case Reconnect =>
        Stream.emit(None)
      case InvalidSession(resumable) =>
        Stream.eval_(if (resumable) IO.unit else state.sessionId.set(none)) ++ Stream.sleep_(5.seconds) ++ Stream.emit(None)
      case Dispatch(nextSequenceNumber, event) =>
        (event match {
          case Ready(_, _, id, _) => Stream.eval_(state.sessionId.set(id.some))
          case _                  => Stream.empty
        }) ++ Stream.eval_(state.sequenceNumber.set(nextSequenceNumber.some)) ++ Stream.emit(Some(event))
    }

  private def identifyOrResume(sessionId: SessionId, sequenceNumber: SequenceNumber, intents: List[Intent]): IO[Text] =
    sessionId.get.flatMap {
      case None     => identifyMessage(intents).pure[IO]
      case Some(id) => sequenceNumber.get.map(s => resumeMessage(id, s))
    }

  def checkForGracefulClose(closeFrame: Close): Either[Throwable, Unit] =
    closeFrame match {
      case Close(1000, _) =>
        ().asRight
      case Close(status, reason) =>
        ConnectionClosedWithError(status, reason).asLeft
    }

  private def heartbeat(interval: FiniteDuration, connection: WSConnectionHighLevel[IO], sequenceNumber: SequenceNumber, acks: Acks): Stream[IO, Unit] = {
    val sendHeartbeat = makeHeartbeat(sequenceNumber).flatMap(connection.send)
    val heartbeats    = Stream.eval(sendHeartbeat) ++ Stream.repeatEval(sendHeartbeat).metered(interval)

    // TODO: Something besides true, false
    (heartbeats.as(true) merge acks.dequeue.as(false)).zipWithPrevious.flatMap {
      case (Some(true), true) => Stream.raiseError[IO](NoHeartbeatAck)
      case _                  => Stream.emit(())
    }
  }

  private def makeHeartbeat(sequenceNumber: SequenceNumber) =
    sequenceNumber.get.map(Heartbeat.apply).map(heartbeat => Text(heartbeat.asJson.noSpaces))

  private def identifyMessage(intents: List[Intent]) = // TODO: Make a case class for the op, d structure that is also used in ControlMessage
    Text(
      Json
        .obj(
          "op" -> 2.asJson,
          "d"  -> Identify(token, IdentifyConnectionProperties("", "", ""), None, None, None, None, None, intents).asJson
        )
        .noSpaces
    )

  private def resumeMessage(sessionId: String, sequenceNumber: Option[Int]) =
    Text(s"""{"op":6,"d":{"token":"$token","session_id":"$sessionId","seq":"$sequenceNumber"}}""")
}

object Discord {
  def make(token: String)(implicit cs: ContextShift[IO], t: Timer[IO]): Resource[IO, Discord] =
    Resource.liftF(utils.javaClient.map(javaClient => new Discord(token, JdkHttpClient[IO](javaClient), JdkWSClient[IO](javaClient))))

  val apiEndpoint                           = uri"https://discordapp.com/api"
  def headers(token: String): Authorization = Authorization(Credentials.Token("Bot".ci, token))

  type SequenceNumber = Ref[IO, Option[Int]]
  type SessionId      = Ref[IO, Option[String]]
  type Acks           = Queue[IO, Unit]

  case class DiscordState(sequenceNumber: SequenceNumber, sessionId: SessionId, acks: Acks)
}
