package dissonance.data.commands

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

case class ApplicationCommandOption(
    `type`: Int,
    name: String,
    description: String,
    required: Boolean,                             // TODO: This says default is false
    choices: List[ApplicationCommandOptionChoice], // TODO: optional but make empty list max 25
    options: List[ApplicationCommandOption]        // TODO: optional but make empty list
)

object ApplicationCommandOption {
  implicit val applicationCommandOptionDecoder: Decoder[ApplicationCommandOption] = deriveDecoder
  implicit val applicationCommandOptionEncoder: Encoder[ApplicationCommandOption] = deriveEncoder
}
