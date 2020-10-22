package dissonance.data.user

import dissonance.data.DiscordId
import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class User(
    id: DiscordId,
    username: String,
    discriminator: String,
    avatar: Option[String],
    bot: Option[Boolean],
    system: Option[Boolean],
    mfaEnabled: Option[Boolean],
    locale: Option[String],
    verified: Option[Boolean],
    email: Option[String],
    flags: List[Role],
    premiumType: Option[PremiumType],
    publicFlags: List[Role]
)

object User {
  implicit val config: Configuration      = Configuration.default.withSnakeCaseMemberNames
  implicit val userDecoder: Decoder[User] = deriveConfiguredDecoder
  implicit val userEncoder: Encoder[User] = deriveConfiguredEncoder
}
