package dissonance.data

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder, Json}

case class ApplicationCommandInteractionDataOption(
    name: String,
    value: Option[Json], // TODO: Make this smarter since it's type is specified by the command, custom decoder?
    options: Option[List[ApplicationCommandInteractionDataOption]]
)

object ApplicationCommandInteractionDataOption {
  implicit val applicationCommandInteractionDataOptionDecoder: Decoder[ApplicationCommandInteractionDataOption] = deriveDecoder
  implicit val applicationCommandInteractionDataOptionEncoder: Encoder[ApplicationCommandInteractionDataOption] = deriveEncoder
}
