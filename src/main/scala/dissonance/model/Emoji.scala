package dissonance.model

import dissonance.model.user.User
import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class Emoji(
    id: Option[Snowflake],
    name: Option[String],
    roles: Option[List[guild.Role]],
    user: Option[User],
    requireColons: Option[Boolean],
    managed: Option[Boolean],
    animated: Option[Boolean],
    available: Option[Boolean]
)

object Emoji {
  implicit val config: Configuration        = Configuration.default.withSnakeCaseMemberNames
  implicit val emojiDecoder: Decoder[Emoji] = deriveConfiguredDecoder
  implicit val emojiEncoder: Encoder[Emoji] = deriveConfiguredEncoder
}
