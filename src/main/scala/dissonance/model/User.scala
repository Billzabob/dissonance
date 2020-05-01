package dissonance.model

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class User(
    id: DiscordId,
    username: String,
    discriminator: String
)

object User {
  implicit val config: Configuration         = Configuration.default.withSnakeCaseMemberNames
  implicit val idDecoder: Decoder[DiscordId] = Decoder[Long].map(DiscordId.apply)
  implicit val messageDecoder: Decoder[User] = deriveConfiguredDecoder
}
