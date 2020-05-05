package dissonance.model.message

import dissonance.model.channel
// import dissonance.model.embed.Embed
import dissonance.model.guild
import dissonance.model.Snowflake
import dissonance.model.user.User
import io.circe.{Decoder, Json}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import java.time.OffsetDateTime

case class Message(
    id: Snowflake,
    channelId: Snowflake,
    guildId: Option[Snowflake],
    author: User,
    member: Option[guild.Member],
    content: String,
    timestamp: OffsetDateTime,
    editedTimestamp: Option[OffsetDateTime],
    tts: Boolean,
    mentionEveryone: Boolean,
    mentions: List[Json], // TODO
    mentionRoles: List[Snowflake],
    mentionChannels: Option[List[channel.Mention]],
    attachments: List[Attachment],
    // embeds: List[Embed],
    reactions: Option[List[Reaction]],
    nonce: Option[String],
    pinned: Boolean,
    webhookId: Option[Snowflake],
    `type`: MessageType,
    activity: Option[Activity],
    application: Option[Application],
    messageReference: Option[Reference],
    flags: Option[List[MessageFlag]]
)

object Message {
  implicit val config: Configuration            = Configuration.default.withSnakeCaseMemberNames
  implicit val messageDecoder: Decoder[Message] = deriveConfiguredDecoder
}
