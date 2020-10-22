package dissonance.data

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import java.time.OffsetDateTime

case class Channel(
    id: Snowflake,
    `type`: ChannelType,
    guildId: Option[Snowflake],
    position: Option[Int],
    permissionOverwrites: Option[List[Overwrite]],
    name: Option[String],
    topic: Option[String],
    nsfw: Option[Boolean],
    lastMessageId: Option[Snowflake],
    bitrate: Option[Int],
    userLimit: Option[Int],
    rateLimitPerUser: Option[Int],
    recipients: Option[List[User]],
    icon: Option[String],
    ownerId: Option[Snowflake],
    applicationId: Option[Snowflake],
    parentId: Option[Snowflake],
    lastPinTimestamp: Option[OffsetDateTime]
)

object Channel {
  implicit val config: Configuration            = Configuration.default.withSnakeCaseMemberNames
  implicit val channelDecoder: Decoder[Channel] = deriveConfiguredDecoder
}
