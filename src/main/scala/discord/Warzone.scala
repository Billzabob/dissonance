package discord

import cats.effect._
import discord.Warzone._
import io.circe._
import io.circe.generic.semiauto._
import org.http4s.Credentials
import org.http4s.Method._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.client.dsl.io._
import org.http4s.headers.Authorization
import org.http4s.implicits._

class Warzone(client: Client[IO], token: String) {

  def checkMatchStatsForUser(battleNetUser: String) = {
    val request = GET(endpoint.addPath(battleNetUser).withQueryParam("type", "wz"), headers(token))
    client.expect[Response](request).map(_.`match`.map(_.segments.head.stats.teamPlacement.displayValue))
  }
}

object Warzone {
  val token = "0ae6b064-5b74-4401-be8d-f42b00e9decb"

  case class Response(`match`: Option[Match])
  case class Match(segments: List[Segment])
  case class Segment(stats: Stats)
  case class Stats(kills: Stat, damageDone: Option[Stat], teamPlacement: Stat)
  case class Stat(value: Double, displayValue: String)

  implicit val statDecoder: Decoder[Stat]         = deriveDecoder[Stat]
  implicit val statsDecoder: Decoder[Stats]       = deriveDecoder[Stats]
  implicit val segmentDecoder: Decoder[Segment]   = deriveDecoder[Segment]
  implicit val matchDecoder: Decoder[Match]       = deriveDecoder[Match]
  implicit val responseDecoder: Decoder[Response] = _.downField("data").get[Json]("matches").map(_.hcursor.downArray.as[Match].toOption).map(Response)

  val endpoint               = uri"https://api.tracker.gg/api/v1/warzone/matches/battlenet"
  def headers(token: String) = Authorization(Credentials.Token("TRN-Api-Key".ci, token))
}
