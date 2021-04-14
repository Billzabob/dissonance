package dissonance.data.commands

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import dissonance.data.util.CirceUtils._

case class ApplicationCommandOptionChoice(name: String, value: Either[Int, String])

object ApplicationCommandOptionChoice {
  implicit val applicationCommandOptionChoiceDecoder: Decoder[ApplicationCommandOptionChoice] = deriveDecoder
  implicit val applicationCommandOptionChoiceEncoder: Encoder[ApplicationCommandOptionChoice] = deriveEncoder
}
