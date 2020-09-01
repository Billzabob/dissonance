package dissonance

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import dissonance.model.Event.MessageCreate
import dissonance.model.intents.Intent
import dissonance.model.message.BasicMessage
import dissonance.model.Event
import fs2.Stream
import io.circe.Json
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.implicits._
import scala.concurrent.duration._

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val token = args.head
    Discord
      .make(token)
      .use { discord =>
        val foo = isPhilPlaying(discord.client.client).evalMap { playing =>
          if (playing) {
            discord.client.sendMessage("Phil is playing League", 602899721912188950L).void
          } else IO.unit
        }

        val bar = discord.subscribe(Intent.GuildMessages).void

        Stream(foo, bar).parJoinUnbounded.compile.drain
      }
      .as(ExitCode.Success)
  }

  def isPhilPlaying(client: Client[IO]): Stream[IO, Boolean] = {
    Stream
      .repeatEval {
        client.expect[Json](
          Request[IO](
            uri = uri"https://na1.api.riotgames.com/lol/match/v4/matchlists/by-account/iMaXll1L6hakH-KuVWi5WfmSi9y58Chx6xzzbLNfy5OPMg",
            headers = Headers.of(Header("X-Riot-Token", "RGAPI-4d83aa3d-5904-4245-a393-00bc753b98db"))
          )
        )
      }
      .metered(1.minute)
      .map(_.hcursor.downField("matches").downArray.get[Long]("gameId").toOption.get)
      .zipWithNext
      .map { case (gameId, gameId2) => gameId.some != gameId2 }
  }

  def handleEvents(discordClient: DiscordClient): Event => IO[Unit] = {
    case MessageCreate(BasicMessage("ping", _, channelId)) => discordClient.sendMessage("pong", channelId).void
    case _                                                 => IO.unit
  }
}
