package dissonance

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.all._
import dissonance.model.Event.MessageCreate
import dissonance.model.Event.MessageReactionAdd
import dissonance.model._
import dissonance.model.embed.{Embed, Field, Image}
import dissonance.model.intents.Intent
import dissonance.model.message.BasicMessage
import dissonance.model.phil.{League, Match}
import dissonance.model.{Color, Event}
import fs2.{CompositeFailure, Stream}
import io.circe.Json
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.implicits._
import scala.concurrent.duration._

object Main extends IOApp {

  val accountId                   = "PPkuQSbweA7FEuJUoxhsaNAsOcLncMW8ML1Z0saBo5ge2A"
  val riotToken                   = "RGAPI-4f0b728b-be4d-4ff3-a95a-13af38b05bad"
  val summonerId                  = "xe_nXiv9w05cVfsfqi580a0JheXzHiAUZIwN0msY9O_VZig"
  val iveGotNothingChannelId      = 602899721912188950L
  val literallyJustNudesChannelId = 605792224759775282L

  override def run(args: List[String]): IO[ExitCode] = {
    val token = args.head
    Discord
      .make(token)
      .use { discord =>
        val notifyPhilPlaying =
          philsGames(discord.httpClient)
            .evalTap(game => sendGameInfo(game, discord.client, literallyJustNudesChannelId))
            .map(_.rank)
            .zipWithPrevious
            .debug()
            .collect { case (Some(a), b) if a != b => (a, b) }
            .evalTap { case (previousRank, newRank) => sendRankInfo(previousRank, newRank, discord.client, iveGotNothingChannelId) }
            .mask
            .repeat

        val events = discord
          .subscribe(Intent.GuildMessages, Intent.GuildMessageReactions)
          .evalMap(handleEvents(discord.client, discord.httpClient))
          .handleErrorWith {
            case f: CompositeFailure => Stream.eval_(IO(println("Stream composite\n" + f.all.map(_.getMessage).intercalate("\n"))))
            case e                   => Stream.eval_(IO(println("Stream " + e)))
          }
          .repeat

        Stream(notifyPhilPlaying, events.void).parJoinUnbounded.compile.drain
      }
      .as(ExitCode.Success)
  }

  def philsGames(client: Client[IO]): Stream[IO, GameInfo] = {
    Stream
      .repeatEval(getMostRecentGame(client))
      .metered(1.minute)
      .zipWithPrevious
      .collect { case (Some(a), b) if a.game.gameId != b.game.gameId => b }
  }

  def sendGameInfo(gameInfo: GameInfo, client: DiscordClient, channelId: Long): IO[Unit] = {
    val player = gameInfo.game.player(accountId)

    import player._

    val baseEmbed = Embed.make
      .withThumbnail(Image(Some(gameInfo.champImage), None, None, None))
      .addFields(
        Field("KDA", s"${stats.kills}/${stats.deaths}/${stats.assists}", false.some),
        Field("DMG%", "%.1f%%".format(gameInfo.game.damagePercentage(accountId) * 100), true.some),
        Field("CSPM", "%.1f".format((stats.totalMinionsKilled + stats.neutralMinionsKilled) * 60 / gameInfo.game.gameDuration.toDouble), true.some),
        Field("GPM", (stats.goldEarned * 60 / gameInfo.game.gameDuration).toString, true.some),
        Field("Vision Score", stats.visionScore.toString, false.some),
        Field("Champ Level", stats.champLevel.toString, false.some),
        Field("Rank", gameInfo.rank, false.some),
        Field("LP", gameInfo.lp.toString, false.some)
      )

    val embedWithBadge = stats.bestAchievement.fold(baseEmbed) { badge =>
      val word = if (stats.win) "and" else "but"
      baseEmbed.withDescription(s"...$word he got $badge")
    }

    val rankedString = if (gameInfo.game.queueId == 420 || gameInfo.game.queueId == 440) "ranked game" else "dumb casual game"

    val embed = if (stats.win) {
      embedWithBadge
        .withColor(Color.green)
        .withTitle("Phil WON a " + rankedString)
    } else {
      embedWithBadge
        .withColor(Color.red)
        .withTitle("Phil LOST a " + rankedString)
    }

    client.sendEmbed(embed, channelId).void
  }

  def sendRankInfo(previousRank: String, newRank: String, client: DiscordClient, channelId: Long): IO[Unit] = {
    val embed = Embed.make
      .withTitle("PHIL'S RANK HAS CHANGED")
      .withColor(Color.blue)
      .addFields(
        Field("Before", previousRank, false.some),
        Field("Now", newRank, false.some)
      )
    client.sendEmbed(embed, channelId).void
  }

  case class GameInfo(game: Match, rank: String, lp: Int, champImage: Uri)

  def getMostRecentGame(client: Client[IO]): IO[GameInfo] = for {
    json       <- riotApiRequest[Json](s"match/v4/matchlists/by-account/$accountId", client)
    gameId     <- json.hcursor.downField("matches").downArray.get[Long]("gameId").liftTo[IO]
    game       <- riotApiRequest[Match](s"match/v4/matches/$gameId", client)
    (rank, lp) <- getRankAndLP(client)
    champImage <- getChampionImageUrlForId(game.player(accountId).championId, client)
  } yield GameInfo(game, rank, lp, champImage)

  def getRankAndLP(client: Client[IO]): IO[(String, Int)] = for {
    leagues <- riotApiRequest[List[League]](s"league/v4/entries/by-summoner/$summonerId", client)
    league  <- leagues.find(_.queueType == "RANKED_SOLO_5x5").liftTo[IO](new Throwable("Player has never played Ranked Solo 5v5"))
  } yield league.tier + " " + league.rank -> league.leaguePoints

  def riotApiRequest[A](path: String, client: Client[IO])(implicit decoder: EntityDecoder[IO, A]): IO[A] =
    client
      .expect[A](
        Request[IO](
          uri = uri"https://na1.api.riotgames.com/lol".addPath(path),
          headers = Headers.of(Header("X-Riot-Token", riotToken))
        )
      )
      .handleErrorWith(e => IO(println(s"Error getting from $path:\n$e")) *> IO.raiseError(e))

  def getChampionImageUrlForId(id: Int, client: Client[IO]): IO[Uri] = for {
    json <- client
              .expect[Json](Request[IO](uri = uri"http://ddragon.leagueoflegends.com/cdn/10.19.1/data/en_US/champion.json"))
              .handleErrorWith(e => IO(println(s"Error getting data dragon:\n$e")) *> IO.raiseError(e))
    keys       <- json.hcursor.downField("data").keys.liftTo[IO](new Throwable("data field was not an object somehow"))
    maybeKey   <- keys.toList.findM(key => json.hcursor.downField("data").downField(key).get[String]("key").map(_ == id.toString)).liftTo[IO]
    key        <- maybeKey.liftTo[IO](new Throwable(s"Could not find champion data with id: $id"))
    champImage <- json.hcursor.downField("data").downField(key).downField("image").get[String]("full").liftTo[IO]
  } yield uri"http://ddragon.leagueoflegends.com/cdn/10.19.1/img/champion" / champImage

  def handleEvents(discordClient: DiscordClient, httpClient: Client[IO]): Event => IO[Unit] = {
    case MessageCreate(BasicMessage(_, "phildo", _, channelId)) => getMostRecentGame(httpClient).flatMap(game => sendGameInfo(game, discordClient, channelId))
    case MessageCreate(BasicMessage(_, "philbo", _, channelId)) => sendRankInfo("fake rank 1", "fake rank 2", discordClient, channelId)
    case MessageCreate(m) if m.embeds.exists(_.title.exists(_.contains("RANK HAS CHANGED"))) =>
      List("🇵", "🇴", "🇬", ":smug_phil:756646030744748032", ":pepe_jedi:673963292477358102", ":aww_yeah:674324758082355291", ":cat_dance:673961665309442048").traverse_(emoji =>
        discordClient.createReaction(m.channelId, m.id, emoji) *> IO.sleep(200.millis)
      )
    case MessageReactionAdd(_, channelId, messageId, _, _, BasicEmoji("🤪")) =>
      discordClient.getChannelMessage(channelId, messageId).flatMap { message =>
        val spongebobText = message.content.zipWithIndex.toList.map {
          case (c, i) if i % 2 == 0 => c.toLower
          case (c, _) => c.toUpper
        }.mkString
        discordClient.sendMessage(spongebobText, channelId).void
      }
    case _ => IO.unit
  }
}
