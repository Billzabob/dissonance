package dissonance.model.activity

import dissonance.model.Timestamp
import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class Timestamps(start: Timestamp, end: Timestamp)

object Timestamps {
  implicit val config: Configuration                  = Configuration.default.withSnakeCaseMemberNames
  implicit val timestampsDecoder: Decoder[Timestamps] = deriveConfiguredDecoder
  implicit val timestampsEncoder: Encoder[Timestamps] = deriveConfiguredEncoder
}
