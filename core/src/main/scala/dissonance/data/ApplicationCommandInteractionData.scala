package dissonance.data

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

case class ApplicationCommandInteractionData(
    id: Snowflake,
    name: String,
    options: Option[List[ApplicationCommandInteractionDataOption]]
)

object ApplicationCommandInteractionData {
  implicit val applicationCommandInteractionDataDecoder: Decoder[ApplicationCommandInteractionData] = deriveDecoder
  implicit val applicationCommandInteractionDataEncoder: Encoder[ApplicationCommandInteractionData] = deriveEncoder
}
