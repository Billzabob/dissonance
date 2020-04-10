package discord

import App.ConnectionPool
import cats.effect._
import cats.effect._
import cats.implicits._
import discord.model._
import java.net.http.HttpClient
import natchez.Trace.Implicits.noop
import org.http4s.client.jdkhttpclient._
import skunk._
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
    val app = new App(client, pool, wz)
    stream.evalMap(app.handleEvent)
  }

  val clients =
    IO(HttpClient.newHttpClient).map(client => (JdkHttpClient[IO](client), JdkWSClient[IO](client)))

  val pool: Resource[IO, Resource[IO, Session[IO]]] =
    Session.pooled(
      host = "localhost",
      port = 5432,
      user = "nhallstrom",
      database = "public",
      password = None,
      max = 10
    )
}
