package dissonance.data

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class MessageActivity(
    `type`: MessageActivityType,
    partyId: Option[String]
)

object MessageActivity {
  implicit val config: Configuration                            = Configuration.default.withSnakeCaseMemberNames
  implicit val messageActivityDecoder: Decoder[MessageActivity] = deriveConfiguredDecoder
}
