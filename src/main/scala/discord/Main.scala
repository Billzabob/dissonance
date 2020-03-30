package discord

import cats.effect._
import cats.effect.concurrent._
import cats.implicits._
import discord.utils._
import discord.model.GetGatewayResponse
import discord.model._
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

object Main extends IOApp with CirceEntityDecoder {
  override def run(args: List[String]): IO[ExitCode] = {
    val clients = IO(HttpClient.newHttpClient).map(client => (JdkHttpClient[IO](client), JdkWSClient[IO](client)))

    clients.flatMap {
      case (client, wsClient) =>
        for {
          uri         <- getUri(client)
          heartbeater <- Heartbeater.make
          _           <- processEvents(wsClient, client, uri, heartbeater)
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

  def processEvents(wsClient: WSClient[IO], client: Client[IO], uri: Uri, h: Heartbeater): IO[Unit] =
    Stream
      .resource(wsClient.connectHighLevel(WSRequest(uri, Headers.of(headers))))
      .flatMap(events(h))
      .evalMap(handleEvent(client))
      .compile
      .drain

  def events(heartbeater: Heartbeater)(connection: WSConnectionHighLevel[IO]): Stream[IO, DispatchEvent] = {
    connection.receiveStream
      .collect {
        // Will always be text since we request JSON encoding
        case Text(data, _) => data
      }
      .map(decode[Event])
      .rethrow
      .map {
        case Hello(interval) =>
          Stream.eval(connection.send(identityMessage)).drain ++ heartbeater.heartbeat(interval, connection).drain
        case HeartBeatAck =>
          Stream.eval(heartbeater.receivedAck).drain
        case Heartbeat(d) =>
          Stream.eval(putStrLn[IO](s"Heartbeat received: $d")).drain
        case Dispatch(nextSequenceNumber, event) =>
          Stream.eval(heartbeater.updateSequenceNumber(nextSequenceNumber)).drain ++ Stream.emit(event)
      }
      .parJoinUnbounded
      .interruptWhen(connection.closeFrame.get.map(checkForGracefulClose))
      .interruptWhen(heartbeater.flatlined)
  }

  def checkForGracefulClose(closeFrame: Close): Either[Throwable, Unit] = closeFrame match {
    case Close(1000, _) =>
      ().asRight
    case Close(status, reason) =>
      new Exception(s"Connection closed with error: $status $reason").asLeft
  }

  def handleEvent(client: Client[IO])(event: DispatchEvent) = event match {
    case Ready(_, _, _) =>
      putStrLn[IO]("Ready received")
    case GuildCreate(_) =>
      putStrLn[IO]("Guild create received")
    case MessageCreate(json) =>
      val cursor = json.hcursor
      (cursor.get[String]("channel_id"), cursor.get[String]("content")).tupled.liftTo[IO].flatMap { case (channel, message) =>
        if (message == "ping")
          client.expect[Json](POST(
            Json.obj("content" -> "pong".asJson),
            apiUri.addPath(s"channels/$channel/messages"),
            headers
          )).map(_.spaces2SortKeys).flatMap(putStrLn[IO])
        else {
          IO.unit
        }
      }
    case TypingStart(_) =>
      putStrLn[IO]("Typing start received")
    case ReactionAdd(_) =>
      putStrLn[IO]("Reaction add received")
    case PresenceUpdate(_) =>
      putStrLn[IO]("Presence update received")
  }

  val token =
    "Njc5NzY4MTU0NjcwNDk3OTA1.XnBLgA.J1aQdB5Kk15QE3faPBRPGePsUfI"

  val identityMessage =
    Text(s"""{"op":2,"d":{"token":"$token","properties":{"$$os":"","$$browser":"","$$device":""}}}""")

  val headers =
    Authorization(Credentials.Token("Bot".ci, token))

  type SequenceNumber = Ref[IO, Option[Int]]
  type AckReceived    = Ref[IO, Boolean]
  type ConnectionDead = Deferred[IO, Unit]
}
