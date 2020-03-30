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
import scala.util.control.NoStackTrace

object Main extends IOApp with CirceEntityDecoder {
  override def run(args: List[String]): IO[ExitCode] = {
    val clients = IO(HttpClient.newHttpClient).map(client => (JdkHttpClient[IO](client), JdkWSClient[IO](client)))

    clients.flatMap {
      case (client, wsClient) =>
        for {
          uri            <- getUri(client)
          sequenceNumber <- Ref[IO].of(none[Int])
          acks           <- SignallingRef[IO, Unit](())
          _              <- processEvents(wsClient, client, uri, sequenceNumber, acks)
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

  def processEvents(wsClient: WSClient[IO], client: Client[IO], uri: Uri, sequenceNumber: SequenceNumber, acks: AckSignal): IO[Unit] =
    Stream
      .resource(wsClient.connectHighLevel(WSRequest(uri, Headers.of(headers))))
      .flatMap(events(sequenceNumber, acks))
      .evalMap(handleEvent(client))
      .compile
      .drain

  def events(sequenceNumber: SequenceNumber, acks: AckSignal)(connection: WSConnectionHighLevel[IO]): Stream[IO, DispatchEvent] = {
    connection.receiveStream
      .collect {
        // Will always be text since we request JSON encoding
        case Text(data, _) => data
      }
      .map(decode[Event])
      .rethrow
      .map {
        case Hello(interval) =>
          Stream.eval_(connection.send(identityMessage)) ++ heartbeat(interval, connection, sequenceNumber, acks).drain
        case HeartBeatAck =>
          Stream.eval_(putStrLn[IO]("Heartbeat ack received") >> acks.set(()))
        case Heartbeat(d) =>
          Stream.eval_(putStrLn[IO](s"Heartbeat received: $d"))
        case Dispatch(nextSequenceNumber, event) =>
          Stream.eval_(sequenceNumber.set(nextSequenceNumber.some)) ++ Stream.emit(event)
      }
      .parJoinUnbounded
      .interruptWhen(connection.closeFrame.get.map(checkForGracefulClose))
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
        client
          .expect[Json](POST(Json.obj("content" -> "pong".asJson), apiUri.addPath(s"channels/${message.channelId}/messages"), headers))
          .map(_.spaces2SortKeys)
          .flatMap(putStrLn[IO])
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
    val heartbeats = Stream.eval(sendHeartbeat) ++ Stream.repeatEval(sendHeartbeat).metered(interval)

    (heartbeats.as(true) merge acks.discrete.as(false)).sliding(2).map(_.toList).flatMap {
      case List(true, true) => Stream.raiseError[IO](NoHeartbeatAck)
      case _ => Stream.emit(())
    }
  }

  def makeHeartbeat(sequenceNumber: SequenceNumber) =
    sequenceNumber.get.map(Heartbeat.apply).map(heartbeat => Text(heartbeat.asJson.noSpaces))

  val identityMessage =
    Text(s"""{"op":2,"d":{"token":"$token","properties":{"$$os":"","$$browser":"","$$device":""}}}""")

  val headers =
    Authorization(Credentials.Token("Bot".ci, token))

  // TODO: Make these wrappers around these types for easier usage
  type SequenceNumber = Ref[IO, Option[Int]]
  type AckSignal = SignallingRef[IO, Unit]
  type AckSignal2 = Signal[IO, Unit]

  case object NoHeartbeatAck extends Throwable with NoStackTrace
  case class ConnectionClosedWithError(statusCode: Int, reason: String) extends Throwable with NoStackTrace
}
