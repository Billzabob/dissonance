package dissonance.model.message

import dissonance.model.Emoji
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class Reaction(
    count: Int,
    me: Boolean,
    emoji: Emoji
)

object Reaction {
  implicit val config: Configuration              = Configuration.default.withSnakeCaseMemberNames
  implicit val reactionDecoder: Decoder[Reaction] = deriveConfiguredDecoder
}
