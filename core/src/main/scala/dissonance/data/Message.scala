package dissonance.data

import io.circe.{Decoder, Json}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import java.time.OffsetDateTime

case class Message(
    id: Snowflake,
    channelId: Snowflake,
    guildId: Option[Snowflake],
    author: User,
    member: Option[Member],
    content: String,
    timestamp: OffsetDateTime,
    editedTimestamp: Option[OffsetDateTime],
    tts: Boolean,
    mentionEveryone: Boolean,
    mentions: List[Json], // TODO
    mentionRoles: List[Snowflake],
    mentionChannels: Option[List[Mention]],
    attachments: List[Attachment],
    embeds: List[Embed],
    reactions: Option[List[Reaction]],
    nonce: Option[String],
    pinned: Boolean,
    webhookId: Option[Snowflake],
    `type`: MessageType,
    activity: Option[MessageActivity],
    application: Option[Application],
    messageReference: Option[Reference],
    flags: Option[List[MessageFlag]],
    referencedMessage: Option[Message]
)

object Message {
  implicit val config: Configuration            = Configuration.default.withSnakeCaseMemberNames
  implicit val messageDecoder: Decoder[Message] = deriveConfiguredDecoder
}
