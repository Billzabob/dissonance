package dissonance.data

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class Field(name: String, value: String, inline: Option[Boolean])

object Field {
  implicit val config: Configuration        = Configuration.default.withSnakeCaseMemberNames
  implicit val fieldDecoder: Decoder[Field] = deriveConfiguredDecoder
  implicit val fieldEncoder: Encoder[Field] = deriveConfiguredEncoder
}
