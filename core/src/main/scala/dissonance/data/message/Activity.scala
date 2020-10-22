package dissonance.data.message

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class Activity(
    `type`: MessageActivityType,
    partyId: Option[String]
)

object Activity {
  implicit val config: Configuration                     = Configuration.default.withSnakeCaseMemberNames
  implicit val messageActivityDecoder: Decoder[Activity] = deriveConfiguredDecoder
}
