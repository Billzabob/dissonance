package dissonance.model.gateway

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class SessionStartLimit(
    total: Int,
    remaining: Int,
    resetAfter: Int
)

object SessionStartLimit {
  implicit val config: Configuration               = Configuration.default.withSnakeCaseMemberNames
  implicit val decoder: Decoder[SessionStartLimit] = deriveConfiguredDecoder
}
