package dissonance

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import dissonance.model.{Color, Event}
import dissonance.model.Event.MessageCreate
import dissonance.model.embed.{Embed, Field, Image}
import dissonance.model.intents.Intent
import dissonance.model.message.BasicMessage
import dissonance.model.phil.Match
import fs2.{CompositeFailure, Stream}
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
        val notifyPhilPlaying =
          philsGames(discord.client.client)
            .evalMap(game => sendGameInfo(game, discord.client, 602899721912188950L))
            .handleErrorWith(e => Stream.eval_(IO(println("Client " + e))))
            .repeat

        val events = discord
          .subscribe(Intent.GuildMessages)
          .evalMap(handleEvents(discord.client))
          .handleErrorWith {
            case f: CompositeFailure => Stream.eval_(IO(println("Stream composite\n" + f.all.foldMap(_.getMessage))))
            case e                   => Stream.eval_(IO(println("Stream " + e)))
          }
          .repeat

        Stream(notifyPhilPlaying, events.void).parJoinUnbounded.compile.drain
      }
      .as(ExitCode.Success)
  }

  def philsGames(client: Client[IO]): Stream[IO, Match] = {
    Stream
      .repeatEval(getMostRecentGame(client))
      .metered(1.minute)
      .zipWithNext
      .flatMap { case (game, game2) =>
        if (game.gameId != game2.get.gameId) Stream.emit(game2.get) else Stream.empty
      }
  }

  val accountId = "PPkuQSbweA7FEuJUoxhsaNAsOcLncMW8ML1Z0saBo5ge2A"
  val riotToken = "RGAPI-4f0b728b-be4d-4ff3-a95a-13af38b05bad"

  def getChampionImageUrlForId(id: Int, client: Client[IO]): IO[Uri] = {
    client
      .expect[Json](
        Request[IO](
          uri = uri"http://ddragon.leagueoflegends.com/cdn/9.3.1/data/en_US/champion.json"
        )
      )
      .map { json =>
        json.hcursor
          .downField("data")
          .keys
          .flatMap { keys =>
            keys
              .find { key =>
                json.hcursor.downField("data").downField(key).get[String]("key").toOption == Some(id.toString)
              }
              .flatMap { key =>
                json.hcursor.downField("data").downField(key).downField("image").get[String]("full").toOption.map { champImage =>
                  uri"http://ddragon.leagueoflegends.com/cdn/9.3.1/img/champion" / champImage
                }
              }
          }
          .get
      }
  }

  def sendGameInfo(game: Match, client: DiscordClient, channelId: Long): IO[Unit] = {
    val player = game.player(accountId)

    import player._

    getChampionImageUrlForId(player.championId, client.client).flatMap { champImage =>
      val embed = Embed.make
        .withImage(Image(Some(champImage), None, None, None))
        .addFields(
          Field("kills", stats.kills.toString, true.some),
          Field("deaths", stats.deaths.toString, true.some),
          Field("assists", stats.assists.toString, true.some),
          Field("CSPM", "%.1f".format((stats.totalMinionsKilled + stats.neutralMinionsKilled) * 60 / game.gameDuration.toDouble), false.some),
          Field("GPM", (stats.goldEarned * 60 / game.gameDuration).toString, false.some)
        )

      val winEmbed =
        embed
          .withColor(Color.green)
          .withTitle("Phil just WON a game!")

      val loseEmbed =
        embed
          .withColor(Color.red)
          .withTitle("Phil just LOST a game!")

      client.sendEmbed(if (stats.win) winEmbed else loseEmbed, channelId).void
    }
  }

  def getMostRecentGame(client: Client[IO]): IO[Match] = {
    client
      .expect[Json](
        Request[IO](
          uri = (uri"https://na1.api.riotgames.com/lol/match/v4/matchlists/by-account" / accountId).withQueryParam("beginIndex", 0).withQueryParam("endIndex", 1),
          headers = Headers.of(Header("X-Riot-Token", riotToken))
        )
      )
      .flatMap { json =>
        val gameId = json.hcursor.downField("matches").downArray.get[Long]("gameId").toOption.get
        client.expect[Match](
          Request[IO](
            uri = uri"https://na1.api.riotgames.com/lol/match/v4/matches" / gameId.toString,
            headers = Headers.of(Header("X-Riot-Token", riotToken))
          )
        )
      }
  }

  def handleEvents(discordClient: DiscordClient): Event => IO[Unit] = {
    case MessageCreate(BasicMessage("phildo", _, channelId)) => getMostRecentGame(discordClient.client).flatMap(game => sendGameInfo(game, discordClient, channelId))
    case _                                                     => IO.unit
  }
}
