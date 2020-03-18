package discord.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.Json

case class Payload(
    op: Int,
    d: Json,
    s: Option[Int],
    t: Option[String]
)

object Payload {
  implicit val config: Configuration     = Configuration.default.withSnakeCaseMemberNames
  implicit val decoder: Decoder[Payload] = deriveConfiguredDecoder
  implicit val encoder: Encoder[Payload] = deriveConfiguredEncoder
}
