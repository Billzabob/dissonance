package discord

import cats.effect._
import discord.utils._
import fs2.Stream
import cats.implicits._
import java.net.http.HttpClient
import org.http4s.implicits._
import org.http4s.client.jdkhttpclient._
import org.http4s.client.jdkhttpclient.WSFrame._
import scala.concurrent.duration._
import scala.util.control.NoStackTrace

object Main2 extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    clients.flatMap {
      case (_, wsClient) =>
        connection(wsClient)
          .mask
          .repeat
          .compile
          .drain
          .as(ExitCode.Success)
    }
  }

  def connection(wsClient: WSClient[IO]) =
    Stream
      .resource(wsClient.connectHighLevel(WSRequest(uri"ws://127.0.0.1:8080/wsecho")))
      .flatMap { connection =>
        printResponses(connection).concurrently(
          Stream
            .awakeEvery[IO](1.seconds)
            .evalMap(time => connection.send(Text(s"${time.toSeconds} seconds")))
        )
      }.take(5) ++ Stream.raiseError[IO](new RuntimeException)

  def checkForGracefulClose(closeFrame: Close): Either[Throwable, Unit] = closeFrame match {
    case Close(1000, _) =>
      ().asRight
    case Close(status, reason) =>
      ConnectionClosedWithError(status, reason).asLeft
  }

  case class ConnectionClosedWithError(statusCode: Int, reason: String) extends NoStackTrace

  def printResponses(connection: WSConnectionHighLevel[IO]): Stream[IO, Unit] = {
    connection.receiveStream
      .collect {
        // Will always be text since we request JSON encoding
        case Text(data, _) => data
      }
      .evalMap(putStrLn[IO])
  }

  val clients = IO(HttpClient.newHttpClient).map(client => (JdkHttpClient[IO](client), JdkWSClient[IO](client)))

}
