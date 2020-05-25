package dissonance.model.webhook

import dissonance.model.Snowflake
import dissonance.model.imagedata.ImageDataUri
import dissonance.model.user.User
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

case class Webhook(
    id: Snowflake,
    `type`: WebhookType,
    guildId: Option[Snowflake], // TODO: optional key
    channelId: Snowflake,
    user: Option[User], // TODO: optional key
    name: Option[String],
    avatar: Option[ImageDataUri],
    token: Option[String] // TODO: optional key
)

object Webhook {
  implicit val config: Configuration            = Configuration.default.withSnakeCaseMemberNames
  implicit val messageDecoder: Decoder[Webhook] = deriveConfiguredDecoder
}
