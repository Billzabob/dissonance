package discord.model

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class User(
    id: Long,
    username: String,
    discriminator: String
)

object User {
  implicit val config: Configuration         = Configuration.default.withSnakeCaseMemberNames
  implicit val messageDecoder: Decoder[User] = deriveConfiguredDecoder
}
