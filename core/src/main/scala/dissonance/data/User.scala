package dissonance.data

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
    flags: List[UserRole],
    premiumType: Option[PremiumType],
    publicFlags: List[UserRole]
)

object User {
  implicit val config: Configuration      = Configuration.default.withSnakeCaseMemberNames
  implicit val userDecoder: Decoder[User] = deriveConfiguredDecoder
  implicit val userEncoder: Encoder[User] = deriveConfiguredEncoder
}
