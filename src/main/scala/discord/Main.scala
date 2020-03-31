package discord

import cats.effect._
import cats.effect.concurrent._
import cats.implicits._
import discord.utils._
import discord.model.GetGatewayResponse
import discord.model._
import fs2.concurrent.SignallingRef
import fs2.Stream
import io.circe.parser._
import io.circe.syntax._
import java.net.http.HttpClient
import org.http4s.Method._
import org.http4s._
import org.http4s.circe.CirceEntityDecoder
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.jdkhttpclient.WSFrame._
import org.http4s.client.jdkhttpclient._
import org.http4s.headers._
import org.http4s.implicits._
import io.circe.Json
import org.http4s.client.dsl.io._
import scala.concurrent.duration.FiniteDuration
import fs2.concurrent.Signal
import scala.concurrent.duration._
import scala.util.control.NoStackTrace

object Main extends IOApp with CirceEntityDecoder {
  override def run(args: List[String]): IO[ExitCode] = {
    val clients = IO(HttpClient.newHttpClient).map(client => (JdkHttpClient[IO](client), JdkWSClient[IO](client)))

    clients.flatMap {
      case (client, wsClient) =>
        for {
          uri            <- getUri(client)
          sequenceNumber <- Ref[IO].of(none[Int])
          sessionId      <- Ref[IO].of(none[String])
          acks           <- SignallingRef[IO, Unit](())
          _              <- processEvents(uri, wsClient, sequenceNumber, acks, sessionId).evalMap(handleEvent(client)).compile.drain
        } yield ExitCode.Success
    }
  }

  val apiUri = uri"https://discordapp.com/api"

  def getUri(client: Client[IO]): IO[Uri] =
    client
      .expect[GetGatewayResponse](GET(apiUri.addPath("gateway/bot"), headers))
      .map(_.url)
      .map(Uri.fromString)
      .rethrow
      .map(_.withQueryParam("v", 6).withQueryParam("encoding", "json"))

  def fakeResource(i: Int) = Resource.make(putStrLn[IO](s"Acquiring Resource $i"))(_ => putStrLn[IO](s"Releasing Resource $i"))

  def processEvents(
      uri: Uri,
      wsClient: WSClient[IO],
      sequenceNumber: SequenceNumber,
      acks: AckSignal,
      sessionId: SessionId
  ): Stream[IO, DispatchEvent] =
    Stream
      .resource(fakeResource(1) >> wsClient.connectHighLevel(WSRequest(uri, Headers.of(headers))).flatMap(c => fakeResource(2).as(c)))
      .evalTap(_ => putStrLn[IO]("Connected and ready"))
      .flatMap(events(sequenceNumber, acks, sessionId))
      .handleErrorWith(e => Stream.eval_(putStrLn[IO](e.toString)))
      .repeat

  def events(
      sequenceNumber: SequenceNumber,
      acks: AckSignal,
      sessionId: SessionId
  )(connection: WSConnectionHighLevel[IO]): Stream[IO, DispatchEvent] = {
    connection.receiveStream
      .collect {
        // Will always be text since we request JSON encoding
        case Text(data, _) => data
      }
      .map(decode[Event])
      .rethrow
      .map {
        case Hello(interval) =>
          Stream.eval_(identifyOrResume(sessionId, sequenceNumber).flatMap(connection.send)) ++ heartbeat(interval, connection, sequenceNumber, acks).drain
        case HeartBeatAck =>
          Stream.eval_(putStrLn[IO]("Heartbeat ack received") >> acks.set(()))
        case Heartbeat(d) =>
          Stream.eval_(putStrLn[IO](s"Heartbeat received: $d"))
        case InvalidSession(resumable) =>
          Stream.eval_(putStrLn[IO](s"Invalid session received: $resumable"))
        case Dispatch(_, event @ Ready(_, _, id)) =>
          Stream.eval_(putStrLn[IO](s"Ready received: $id")) ++ Stream.eval_(sessionId.set(id.some)) ++ Stream.emit(event)
        case Dispatch(nextSequenceNumber, event) =>
          Stream.eval_(sequenceNumber.set(nextSequenceNumber.some)) ++ Stream.emit(event)
      }
      .parJoinUnbounded
      .interruptWhen(connection.closeFrame.get.flatTap(c => putStrLn[IO](s"CLOSE RECEIVED: $c")).map(checkForGracefulClose))
  }

  def identifyOrResume(sessionId: SessionId, sequenceNumber: SequenceNumber): IO[Text] = sessionId.get.flatMap {
    case None =>
      identityMessage.pure[IO]
    case Some(id) =>
      sequenceNumber.get.map(s => resumeMessage(id, s))
  }

  def checkForGracefulClose(closeFrame: Close): Either[Throwable, Unit] = closeFrame match {
    case Close(1000, _) =>
      ().asRight
    case Close(status, reason) =>
      ConnectionClosedWithError(status, reason).asLeft
  }

  def handleEvent(client: Client[IO])(event: DispatchEvent): IO[Unit] = event match {
    case Ready(_, _, _) =>
      putStrLn[IO]("Ready received")
    case GuildCreate(_) =>
      putStrLn[IO]("Guild create received")
    case MessageCreate(message) =>
      if (message.content == "ping")
        client.expect[Json](POST(Json.obj("content" -> "pong".asJson), apiUri.addPath(s"channels/${message.channelId}/messages"), headers)).void
      else IO.unit
    case TypingStart(_) =>
      putStrLn[IO]("Typing start received")
    case ReactionAdd(_) =>
      putStrLn[IO]("Reaction add received")
    case PresenceUpdate(_) =>
      putStrLn[IO]("Presence update received")
  }

  val token =
    "Njc5NzY4MTU0NjcwNDk3OTA1.XnBLgA.J1aQdB5Kk15QE3faPBRPGePsUfI"

  def heartbeat(interval: FiniteDuration, connection: WSConnectionHighLevel[IO], sequenceNumber: SequenceNumber, acks: AckSignal2): Stream[IO, Unit] = {
    val sendHeartbeat = putStrLn[IO]("Sending heartbeat") >> makeHeartbeat(sequenceNumber).flatMap(connection.send)
    val heartbeats    = Stream.eval(sendHeartbeat) ++ Stream.repeatEval(sendHeartbeat).metered(interval)

    (heartbeats.as(true) merge acks.discrete.as(false)).sliding(2).map(_.toList).flatMap {
      case List(true, true) => Stream.raiseError[IO](NoHeartbeatAck)
      case _                => Stream.emit(())
    }
  }

  def makeHeartbeat(sequenceNumber: SequenceNumber) =
    sequenceNumber.get.map(Heartbeat.apply).map(heartbeat => Text(heartbeat.asJson.noSpaces))

  val identityMessage =
    Text(s"""{"op":2,"d":{"token":"$token","properties":{"$$os":"","$$browser":"","$$device":""}}}""")

  def resumeMessage(sessionId: String, sequenceNumber: Option[Int]) =
    Text(s"""{"op":6,"d":{"token":"$token","session_id":"$sessionId","seq":"$sequenceNumber"}}""")

  val headers =
    Authorization(Credentials.Token("Bot".ci, token))

  // TODO: Make these wrappers around these types for easier usage
  type SequenceNumber = Ref[IO, Option[Int]]
  type SessionId      = Ref[IO, Option[String]]
  // TODO: Should these be Signals or Queues?
  type AckSignal      = SignallingRef[IO, Unit]
  type AckSignal2     = Signal[IO, Unit]

  case class ConnectionClosedWithError(statusCode: Int, reason: String) extends NoStackTrace
  case class SessionInvalid(resumable: Boolean)                         extends NoStackTrace
  case object NoHeartbeatAck                                            extends NoStackTrace
}
