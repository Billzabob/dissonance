package dissonance.model

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class Emoji(name: String, id: Snowflake, animated: Boolean)

object Emoji {
  implicit val config: Configuration        = Configuration.default.withSnakeCaseMemberNames
  implicit val emojiDecoder: Decoder[Emoji] = deriveConfiguredDecoder
}
