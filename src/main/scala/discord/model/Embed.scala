package discord.model

import cats.data.NonEmptyList
import cats.implicits._
import io.circe.{Encoder, Json}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class Embed(
    title: Option[String],
    `type`: Option[String], // TODO: Enum
    description: Option[String],
    url: Option[String],
    timestamp: Option[String], // TODO: Actual timestamp
    color: Option[Int],        // TODO: Color type
    footer: Option[Json],      // TODO: Not just JSON
    image: Option[Json],
    thumbnail: Option[Json],
    video: Option[Json],
    provider: Option[Json],
    author: Option[Json],
    fields: Option[NonEmptyList[Json]]
) {
  def withTitle(title: String)             = copy(title = title.some)
  def withType(`type`: String)             = copy(`type` = `type`.some)
  def withDescription(description: String) = copy(description = description.some)
  def withUrl(url: String)                 = copy(url = url.some)
  // TODO: Add rest
}

object Embed {
  def make = Embed(None, None, None, None, None, None, None, None, None, None, None, None, None)

  implicit val config: Configuration          = Configuration.default.withSnakeCaseMemberNames
  implicit val messageDecoder: Encoder[Embed] = deriveConfiguredEncoder
}
