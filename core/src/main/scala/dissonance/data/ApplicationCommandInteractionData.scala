package dissonance.data

import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.{Decoder, Encoder}

case class ApplicationCommandInteractionData(
    id: Snowflake,
    name: String,
    options: Option[List[ApplicationCommandInteractionDataOption]]
)

object ApplicationCommandInteractionData {
  implicit val config: Configuration                                                                = Configuration.default.withSnakeCaseMemberNames
  implicit val applicationCommandInteractionDataDecoder: Decoder[ApplicationCommandInteractionData] = deriveConfiguredDecoder
  implicit val applicationCommandInteractionDataEncoder: Encoder[ApplicationCommandInteractionData] = deriveConfiguredEncoder
}
