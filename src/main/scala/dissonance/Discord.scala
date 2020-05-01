package dissonance

import cats.effect._
import cats.effect.concurrent._
import cats.implicits._
import dissonance.Discord._
import dissonance.model._
import dissonance.model.DispatchEvent._
import dissonance.model.Errors._
import dissonance.model.Event._
import dissonance.utils._
import fs2.concurrent.Queue
import fs2.Stream
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

  type EventHandler   = DispatchEvent => IO[Unit]
  type SequenceNumber = Ref[IO, Option[Int]]
  type SessionId      = Ref[IO, Option[String]]
  type Acks           = Queue[IO, Unit]

  def subscribe(eventHandler: EventHandler): IO[Unit] =
    for {
      uri            <- getUri
      sequenceNumber <- Ref[IO].of(none[Int])
      sessionId      <- Ref[IO].of(none[String])
      acks           <- Queue.unbounded[IO, Unit]
      _              <- processEvents(uri, sequenceNumber, acks, sessionId).evalMap(eventHandler).compile.drain
    } yield ()

  private def getUri: IO[Uri] =
    httpClient
      .expect[GetGatewayResponse](GET(apiEndpoint.addPath("gateway/bot"), headers(token)))
      .map(_.url)
      .map(Uri.fromString)
      .rethrow
      .map(_.withQueryParam("v", 6).withQueryParam("encoding", "json"))

  private def processEvents(
      uri: Uri,
      sequenceNumber: SequenceNumber,
      acks: Acks,
      sessionId: SessionId
  ): Stream[IO, DispatchEvent] =
    Stream
      .resource(wsClient.connectHighLevel(WSRequest(uri, Headers.of(headers(token)))))
      .flatMap(connection => events(connection, sequenceNumber, acks, sessionId))
      .repeat

  private def events(
      connection: WSConnectionHighLevel[IO],
      sequenceNumber: SequenceNumber,
      acks: Acks,
      sessionId: SessionId
  ): Stream[IO, DispatchEvent] = {
    connection.receiveStream
      .collect {
        // Will always be text since we request JSON encoding
        case Text(data, _) => data
      }
      .map(decode[Event])
      .flatMap {
        case Right(a) => Stream.emit(a)
        case Left(b)  => Stream.eval_(putStrLn(b.toString))
      }
      // .rethrow // TODO: rethrow instead of flatmap eventually
      .map(event => handleEvents(event, sequenceNumber, acks, sessionId, connection))
      .parJoinUnbounded
      .interruptWhen(connection.closeFrame.get.map(handleConnectionClose))
      .handleErrorWith(e => Stream.eval_(putStrLn(e.toString)))
  }

  private def handleEvents(
      event: Event,
      sequenceNumber: SequenceNumber,
      acks: Acks,
      sessionId: SessionId,
      connection: WSConnectionHighLevel[IO]
  ): Stream[IO, DispatchEvent] = event match {
    case Hello(interval) =>
      Stream.eval_(identifyOrResume(sessionId, sequenceNumber).flatMap(connection.send)) ++ heartbeat(interval, connection, sequenceNumber, acks).drain
    case HeartBeatAck =>
      Stream.eval_(acks.enqueue1(()))
    case Heartbeat(d) =>
      Stream.eval_(putStrLn(s"Heartbeat received: $d"))
    case Reconnect =>
      Stream.raiseError[IO](ReconnectReceived)
    case InvalidSession(resumable) =>
      Stream.eval_(if (resumable) IO.unit else sessionId.set(none)) ++ Stream.sleep_(5.seconds) ++ Stream.raiseError[IO](SessionInvalid(resumable))
    case Dispatch(nextSequenceNumber, event) =>
      (event match {
        case Ready(_, _, id, _) => Stream.eval_(sessionId.set(id.some))
        case _                  => Stream.empty
      }) ++ Stream.eval_(sequenceNumber.set(nextSequenceNumber.some)) ++ Stream.emit(event)
  }

  private def identifyOrResume(sessionId: SessionId, sequenceNumber: SequenceNumber): IO[Text] = sessionId.get.flatMap {
    case None =>
      identityMessage.pure[IO]
    case Some(id) =>
      sequenceNumber.get.map(s => resumeMessage(id, s))
  }

  private def handleConnectionClose(closeFrame: Close): Either[Throwable, Unit] =
    ConnectionClosedWithError(closeFrame.statusCode, closeFrame.reason).asLeft

  private def heartbeat(
      interval: FiniteDuration,
      connection: WSConnectionHighLevel[IO],
      sequenceNumber: SequenceNumber,
      acks: Acks
  ): Stream[IO, Unit] = {
    val sendHeartbeat = makeHeartbeat(sequenceNumber).flatMap(connection.send)
    val heartbeats    = Stream.eval(sendHeartbeat) ++ Stream.repeatEval(sendHeartbeat).metered(interval)

    // TODO: Something besides true, false
    (heartbeats.as(true) merge acks.dequeue.as(false)).sliding(2).map(_.toList).flatMap {
      case List(true, true) => Stream.raiseError[IO](NoHeartbeatAck) // TODO: Terminate connection with non-1000 error code
      case _                => Stream.emit(())
    }
  }

  private def makeHeartbeat(sequenceNumber: SequenceNumber) =
    sequenceNumber.get.map(Heartbeat.apply).map(heartbeat => Text(heartbeat.asJson.noSpaces))

  private def identityMessage =
    Text(s"""{"op":2,"d":{"token":"$token","intents":512,"properties":{"$$os":"","$$browser":"","$$device":""}}}""")

  private def resumeMessage(sessionId: String, sequenceNumber: Option[Int]) =
    Text(s"""{"op":6,"d":{"token":"$token","session_id":"$sessionId","seq":"$sequenceNumber"}}""")
}

object Discord {
  def make(token: String)(implicit cs: ContextShift[IO], t: Timer[IO]): Resource[IO, Discord] =
    Resource.liftF(utils.javaClient.map(javaClient => new Discord(token, JdkHttpClient[IO](javaClient), JdkWSClient[IO](javaClient))))

  val apiEndpoint            = uri"https://discordapp.com/api"
  def headers(token: String) = Authorization(Credentials.Token("Bot".ci, token))
}
