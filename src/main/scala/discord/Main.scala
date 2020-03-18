package discord

import cats.effect._
import cats.effect.concurrent.Ref
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
import org.http4s.client.Client
import org.http4s.client.jdkhttpclient.WSFrame._
import org.http4s.client.jdkhttpclient._
import org.http4s.headers._
import org.http4s.implicits._
import scala.concurrent.duration.FiniteDuration

object Main extends IOApp with CirceEntityDecoder {
  override def run(args: List[String]): IO[ExitCode] = {
    val clients = IO(HttpClient.newHttpClient).map(client => (JdkHttpClient[IO](client), JdkWSClient[IO](client)))

    clients.flatMap {
      case (client, wsClient) =>
        for {
          uri            <- getUri(client)
          sequenceNumber <- Ref[IO].of(none[Int])
          _              <- processEvents(wsClient, uri, sequenceNumber)
        } yield ExitCode.Success
    }
  }

  def getUri(client: Client[IO]): IO[Uri] =
    client
      .expect[GetGatewayResponse](Request[IO](GET, uri"https://discordapp.com/api/gateway/bot", headers = headers))
      .map(_.url)
      .map(Uri.fromString)
      .rethrow
      .map(_.withQueryParam("v", 6).withQueryParam("encoding", "json"))

  def processEvents(wsClient: WSClient[IO], uri: Uri, sequenceNumber: SequenceNumber): IO[Unit] =
    Stream
      .resource(wsClient.connectHighLevel(WSRequest(uri, headers)))
      .flatMap(events(sequenceNumber))
      .evalMap(handleEvent)
      .compile
      .drain

  def events(sequenceNumber: SequenceNumber)(connection: WSConnectionHighLevel[IO]): Stream[IO, DispatchEvent] = {
    connection.receiveStream
      .collect {
        // Will always be text since we request JSON encoding
        case Text(data, _) => data
      }
      .map(decode[Event])
      .rethrow
      .map {
        case Hello(interval) =>
          Stream.eval(putStrLn[IO]("Hello Received") >> connection.send(Text(identity))).drain ++ heartbeat(interval, connection, sequenceNumber).drain
        case HeartBeatAck =>
          Stream.eval(putStrLn[IO]("Heartbeat ack received")).drain // TODO: Handle when Discord stops sending acks
        case Heartbeat(d) =>
          Stream.eval(putStrLn[IO](s"Heartbeat received: $d")).drain
        case Dispatch(nextSequenceNumber, event) =>
          Stream.eval(sequenceNumber.set(nextSequenceNumber.some)).drain ++ Stream.emit(event)
      }
      .parJoinUnbounded
      .interruptWhen(connection.closeFrame.get.map(checkForGracefulClose))
  }

  def checkForGracefulClose(closeFrame: Close): Either[Throwable, Unit] = closeFrame match {
    case Close(1000, _) =>
      ().asRight
    case Close(status, reason) =>
      new Exception(s"Connection closed with error: $status $reason").asLeft
  }

  def handleEvent(event: DispatchEvent) = event match {
    case Ready(_, _, _) =>
      putStrLn[IO]("Ready received")
    case GuildCreate(_) =>
      putStrLn[IO]("Guild create received")
    case MessageCreate(json) =>
      json.hcursor.get[String]("content").liftTo[IO].map(_.toUpperCase).flatMap(putStrLn[IO])
    case TypingStart(_) =>
      putStrLn[IO]("Typing start received")
    case ReactionAdd(_) =>
      putStrLn[IO]("Reaction add received")
  }

  val token =
    "Njc5NzY4MTU0NjcwNDk3OTA1.XnBLgA.J1aQdB5Kk15QE3faPBRPGePsUfI"

  val identity =
    s"""{"op":2,"d":{"token":"$token","properties":{"$$os":"","$$browser":"","$$device":""}}}"""

  def heartbeat(interval: FiniteDuration, connection: WSConnectionHighLevel[IO], sequenceNumber: SequenceNumber): Stream[IO, Unit] = {
    val sendHeartbeat = putStrLn[IO]("Sending heartbeat") >> makeHeartbeat(sequenceNumber).flatMap(connection.send)
    Stream.eval(sendHeartbeat) ++ Stream.repeatEval(sendHeartbeat).metered(interval)
  }

  def makeHeartbeat(sequenceNumber: SequenceNumber) =
    sequenceNumber.get.map(Heartbeat.apply).map(heartbeat => Text(heartbeat.asJson.noSpaces))

  val headers: Headers =
    Headers.of(Authorization(Credentials.Token("Bot".ci, token)))

  type SequenceNumber = Ref[IO, Option[Int]]
}
