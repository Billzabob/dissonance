package dissonance.data

import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.{Decoder, Encoder, Json}

case class ApplicationCommandInteractionDataOption(
    name: String,
    value: Option[Json], // TODO: Make this smarter since it's type is specified by the command, custom decoder?
    options: Option[List[ApplicationCommandInteractionDataOption]]
)

object ApplicationCommandInteractionDataOption {
  implicit val config: Configuration                                                                            = Configuration.default.withSnakeCaseMemberNames
  implicit val applicationCommandInteractionDataOptionDecoder: Decoder[ApplicationCommandInteractionDataOption] = deriveConfiguredDecoder
  implicit val applicationCommandInteractionDataOptionEncoder: Encoder[ApplicationCommandInteractionDataOption] = deriveConfiguredEncoder
}
