package discord

import cats.effect._
import cats.implicits._
import discord.model.GetGatewayResponse
import fs2.Stream
import java.net.http.HttpClient
import io.circe.parser._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.client.jdkhttpclient._
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.Method._
import discord.model.Payload

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val clients = IO(HttpClient.newHttpClient).map(client => (JdkHttpClient[IO](client), JdkWSClient[IO](client)))

    clients.flatMap {
      case (client, wsClient) =>
        for {
          uri <- getUri(client)
          _   <- processEvents(wsClient, uri)
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

  def processEvents(wsClient: WSClient[IO], uri: Uri): IO[Unit] =
    Stream
      .resource(wsClient.connectHighLevel(WSRequest(uri, headers)))
      .flatMap(processEvents)
      .compile
      .drain

  def processEvents(connection: WSConnectionHighLevel[IO]): Stream[IO, Unit] =
    connection.receiveStream
      .collect { case WSFrame.Text(data, _) => data } // Will always be text since we request JSON encoding
      .map(decode[Payload])
      .rethrow
      .map(_.d)
      .take(1)
      .showLinesStdOut

  val headers: Headers =
    Headers.of(Authorization(Credentials.Token("Bot".ci, "Njc5NzY4MTU0NjcwNDk3OTA1.XnBLgA.J1aQdB5Kk15QE3faPBRPGePsUfI")))
}
