package discord

import cats.effect._
import cats.effect._
import cats.implicits._
import discord.DB._
import discord.model._
import java.net.http.HttpClient
import org.http4s.client.jdkhttpclient._
import fs2.Pipe

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    pool
      .use { p =>
        clients.flatMap {
          case (client, wsClient) =>
            val wz = new Warzone(client, args(1))
            Discord(args(0), client, wsClient).start(handleEvents(p, wz))
        }
      }
      .as(ExitCode.Success)

  def handleEvents(pool: ConnectionPool, wz: Warzone)(client: DiscordClient): Pipe[IO, DispatchEvent, Unit] = stream => {
    val db  = new DB(pool)
    val app = new App(client, db, wz)
    stream.evalMap(app.handleEvent)
  }

  val clients =
    IO(HttpClient.newHttpClient).map(client => (JdkHttpClient[IO](client), JdkWSClient[IO](client)))
}
