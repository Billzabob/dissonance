package dissonance.model.activity

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import java.time.Instant

case class Timestamps(start: Instant, end: Instant)

object Timestamps {
  implicit val config: Configuration                  = Configuration.default.withSnakeCaseMemberNames
  implicit val timestampsDecoder: Decoder[Timestamps] = deriveConfiguredDecoder
}
