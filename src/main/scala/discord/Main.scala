package discord

import cats.effect._
import cats.effect.concurrent._
import cats.implicits._
import discord.model._
import discord.model.DispatchEvent._
import discord.model.Event._
import discord.utils._
import fs2.concurrent.Queue
import fs2.Stream
import io.circe.Json
import io.circe.parser._
import io.circe.syntax._
import java.net.http.HttpClient
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.io._
import org.http4s.client.jdkhttpclient._
import org.http4s.client.jdkhttpclient.WSFrame._
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.Method._
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
          acks           <- Queue.unbounded[IO, Unit]
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

  def processEvents(
      uri: Uri,
      wsClient: WSClient[IO],
      sequenceNumber: SequenceNumber,
      acks: Acks,
      sessionId: SessionId
  ): Stream[IO, DispatchEvent] =
    Stream
      .resource(wsClient.connectHighLevel(WSRequest(uri, Headers.of(headers))))
      .flatMap(events(sequenceNumber, acks, sessionId))
      .handleErrorWith(e => Stream.eval_(putStrLn[IO](e.toString)))
      .repeat

  def events(
      sequenceNumber: SequenceNumber,
      acks: Acks,
      sessionId: SessionId
  )(connection: WSConnectionHighLevel[IO]): Stream[IO, DispatchEvent] = {
    connection.receiveStream
      .collect {
        // Will always be text since we request JSON encoding
        case Text(data, _) => data
      }
      .map(decode[Event])
      .rethrow
      .map(handleEvents(sequenceNumber, acks, sessionId, connection))
      .parJoinUnbounded
      .interruptWhen(connection.closeFrame.get.flatTap(c => putStrLn[IO](s"CLOSE RECEIVED: $c")).map(checkForGracefulClose))
  }

  def handleEvents(
      sequenceNumber: SequenceNumber,
      acks: Acks,
      sessionId: SessionId,
      connection: WSConnectionHighLevel[IO]
  )(event: Event): Stream[IO, DispatchEvent] = event match {
    case Hello(interval) =>
      Stream.eval_(identifyOrResume(sessionId, sequenceNumber).flatMap(connection.send)) ++ heartbeat(interval, connection, sequenceNumber, acks).drain
    case HeartBeatAck =>
      Stream.eval_(acks.enqueue1(()))
    case Heartbeat(d) =>
      Stream.eval_(putStrLn[IO](s"Heartbeat received: $d"))
    case Reconnect =>
      Stream.raiseError[IO](ReconnectReceived)
    case InvalidSession(resumable) =>
      Stream.eval_(if (resumable) IO.unit else sessionId.set(none)) ++ Stream.sleep_(5.seconds) ++ Stream.raiseError[IO](SessionInvalid(resumable))
    case Dispatch(nextSequenceNumber, event) =>
      (event match {
        case Ready(_, _, id, _) => Stream.eval_(sessionId.set(id.some))
        case _               => Stream.empty
      }) ++ Stream.eval_(sequenceNumber.set(nextSequenceNumber.some)) ++ Stream.emit(event)
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

  // This is basically what I imagine the user has to implement to use this framework, except with a Discord wrapper around client
  def handleEvent(client: Client[IO])(event: DispatchEvent): IO[Unit] = event match {
    case MessageCreate(message) =>
      if (message.content == "ping")
        client.expect[Json](POST(Json.obj("content" -> "pong".asJson), apiUri.addPath(s"channels/${message.channelId}/messages"), headers)).void
      else IO.unit
    case _ =>
      IO.unit
  }

  val token =
    "Njc5NzY4MTU0NjcwNDk3OTA1.Xovt5w.GI4dnRdi5UnLG7AfAO6QYoZmQLs"

  def heartbeat(interval: FiniteDuration, connection: WSConnectionHighLevel[IO], sequenceNumber: SequenceNumber, acks: Acks): Stream[IO, Unit] = {
    val sendHeartbeat = makeHeartbeat(sequenceNumber).flatMap(connection.send)
    val heartbeats    = Stream.eval(sendHeartbeat) ++ Stream.repeatEval(sendHeartbeat).metered(interval)

    // TODO: Something besides true, false
    (heartbeats.as(true) merge acks.dequeue.as(false)).sliding(2).map(_.toList).flatMap {
      case List(true, true) => Stream.raiseError[IO](NoHeartbeatAck) // TODO: Terminate connection with non-1000 error code
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
  type Acks           = Queue[IO, Unit]

  case class ConnectionClosedWithError(statusCode: Int, reason: String) extends NoStackTrace
  case class SessionInvalid(resumable: Boolean)                         extends NoStackTrace
  case object NoHeartbeatAck                                            extends NoStackTrace
  case object ReconnectReceived                                         extends NoStackTrace
}
