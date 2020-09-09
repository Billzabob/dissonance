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
        val notifyPhilPlaying =
          philsGames(discord.client.client)
            .evalMap { gameId =>
              discord.client.client.expect[Json](
                Request[IO](
                  uri = uri"https://na1.api.riotgames.com/lol/match/v4/matches" / gameId.toString,
                  headers = Headers.of(Header("X-Riot-Token", token))
                )
              )
            }.evalMap { json =>
              val winners = getWinningTeam(json)
              val philsId = getPhilsParticipantId(json)

              val philWon = (winners == 100 && philsId <= 5) || (winners == 200 && philsId >= 6)

              if (philWon) {
                discord.client.sendMessage("Phil just WON a game of League", 602899721912188950L).void
              } else {
                discord.client.sendMessage("Phil just LOST a game of League", 602899721912188950L).void
              }
            }

        val events = discord.subscribe(Intent.GuildMessages).handleErrorWith(e => Stream.eval_(IO(println(e)))).repeat

        Stream(notifyPhilPlaying, events.void).parJoinUnbounded.compile.drain
      }
      .as(ExitCode.Success)
  }

  type GameId = Long

  def philsGames(client: Client[IO]): Stream[IO, Long] = {
    Stream
      .repeatEval {
        client.expect[Json](
          Request[IO](
            uri = uri"https://na1.api.riotgames.com/lol/match/v4/matchlists/by-account" / accountId,
            headers = Headers.of(Header("X-Riot-Token", token))
          )
        )
      }
      .metered(1.minute)
      .map(_.hcursor.downField("matches").downArray.get[Long]("gameId").toOption.get)
      .zipWithNext
      .flatMap { case (gameId, gameId2) =>
        if (gameId != gameId2.get) Stream.emit(gameId2.get) else Stream.empty
      }
  }

  def getWinningTeam(json: Json): Int =
    json.hcursor.downField("teams").as[List[Json]].toOption.get.find(_.hcursor.get[String]("win").exists(_ == "Win")).get.hcursor.get[Int]("teamId").toOption.get

  def getPhilsParticipantId(json: Json): Int =
    json.hcursor.downField("participantIdentities").as[List[Json]].toOption.get.find(_.hcursor.downField("player").get[String]("accountId").exists(_ == accountId)).get.hcursor.get[Int]("participantId").toOption.get

  val accountId = "PPkuQSbweA7FEuJUoxhsaNAsOcLncMW8ML1Z0saBo5ge2A"
  val token = "RGAPI-4f0b728b-be4d-4ff3-a95a-13af38b05bad"

  def handleEvents(discordClient: DiscordClient): Event => IO[Unit] = {
    case MessageCreate(BasicMessage("ping", _, channelId)) => discordClient.sendMessage("pong", channelId).void
    case _                                                 => IO.unit
  }
}
