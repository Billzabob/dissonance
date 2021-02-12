package dissonance.data

import cats.syntax.all._

import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import java.time.OffsetDateTime
import org.http4s.circe._
import org.http4s.Uri

sealed trait Event extends Product with Serializable

object events {
  case class ChannelCreate(channel: Channel)                                                                       extends Event
  case class ChannelDelete(channel: Channel)                                                                       extends Event
  case class ChannelPinsUpdate(guildId: Snowflake, channelId: Snowflake, lastPinTimestamp: Option[OffsetDateTime]) extends Event
  case class ChannelUpdate(channel: Channel)                                                                       extends Event
  case class GuildBanAdd(guildBan: Ban)                                                                            extends Event
  case class GuildBanRemove(guildBan: Ban)                                                                         extends Event
  case class GuildCreate(guild: Guild)                                                                             extends Event
  case class GuildDelete(guild: Guild)                                                                             extends Event
  case class GuildEmojisUpdate(guildId: Snowflake, emojis: List[Emoji])                                            extends Event
  case class GuildId(guildId: Snowflake)                                                                           extends Event
  case class GuildMemberAdd(guildId: Snowflake, member: Member)                                                    extends Event
  case class GuildMembersChunk(
      guildId: Snowflake,
      members: List[Member],
      chunkIndex: Int,
      chunkCount: Int,
      notFound: Option[List[Json]],
      presences: Option[List[Presence]],
      nonce: Option[String]
  )                                                                                                                                                extends Event
  case class GuildMemberRemove(guildId: Snowflake, user: User)                                                                                     extends Event
  case class GuildMemberUpdate(guildId: Snowflake, roles: List[Snowflake], user: User, nick: Option[String], premiumSince: Option[OffsetDateTime]) extends Event
  case class GuildRoleCreate(guildId: Snowflake, role: GuildRole)                                                                                  extends Event
  case class GuildRoleDelete(guildId: Snowflake, roleId: Snowflake)                                                                                extends Event
  case class GuildRoleUpdate(guildId: Snowflake, role: GuildRole)                                                                                  extends Event
  case class GuildUpdate(guild: Guild)                                                                                                             extends Event
  case class InteractionCreate(
    id: Snowflake,
    `type`: InteractionType,
    data: ApplicationCommandInteractionData,
    guild_id: Snowflake,
    channel_id: Snowflake,
    member: Member,
    token: String,
    version: Int
  ) extends Event
  case class InviteCreate(
      channelId: Snowflake,
      code: String,
      createdAt: Timestamp,
      guildId: Option[Snowflake],
      inviter: Option[User],
      maxAge: Int, // TODO: This is in seconds, so convert to Duration?
      maxUses: Int,
      targetUser: Option[User],
      targetUserType: Option[TargetUserType],
      temporary: Boolean,
      uses: Int
  )                                                                                                                                                              extends Event
  case class InviteDelete(channelId: Snowflake, guildId: Option[Snowflake], code: String)                                                                        extends Event
  case class MessageCreate(message: Message)                                                                                                                     extends Event
  case class MessageDelete(id: Snowflake, channelId: Snowflake, guildId: Option[Snowflake])                                                                      extends Event
  case class MessageDeleteBulk(ids: List[Snowflake], channelId: Snowflake, guildId: Option[Snowflake])                                                           extends Event
  case class MessageReactionAdd(userId: Snowflake, channelId: Snowflake, messageId: Snowflake, guildId: Option[Snowflake], member: Option[Member], emoji: Emoji) extends Event
  case class MessageReactionRemove(userId: Snowflake, channelId: Snowflake, messageId: Snowflake, guildId: Option[Snowflake], emoji: Emoji)                      extends Event
  case class MessageReactionRemoveAll(channelId: Snowflake, messageId: Snowflake, guildId: Option[Snowflake])                                                    extends Event
  case class MessageReactionRemoveEmoji(channelId: Snowflake, messageId: Snowflake, guildId: Option[Snowflake], emoji: Emoji)                                    extends Event
  case class MessageUpdate(update: Update)                                                                                                                       extends Event
  case class PresenceUpdate(presence: Presence)                                                                                                                  extends Event
  case class Ready(v: Int, user: User, sessionId: String, shard: Option[(Int, Int)])                                                                             extends Event // TODO: Parse the 2 element array into a case class instead of tuple
  case object Resumed                                                                                                                                            extends Event
  case class TypingStart(channelId: Snowflake, guildId: Option[Snowflake], userId: Snowflake, timestamp: Timestamp, member: Option[Member])                      extends Event
  case class UserUpdate(user: User)                                                                                                                              extends Event
  case class VoiceStateUpdate(voiceState: VoiceState)                                                                                                            extends Event
  case class VoiceServerUpdate(token: String, guildId: Snowflake, endpoint: Uri)                                                                                 extends Event
  case class WebhookUpdate(guildId: Snowflake, channelId: Snowflake)                                                                                             extends Event
}

object Event {
  import events._

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

  implicit val channelPinsUpdateDecoder: Decoder[ChannelPinsUpdate]            = deriveConfiguredDecoder
  implicit val guildEmojisDecoder: Decoder[GuildEmojisUpdate]                  = deriveConfiguredDecoder
  implicit val guildIdDecoder: Decoder[GuildId]                                = deriveConfiguredDecoder
  implicit val guildMemberRemoveDecoder: Decoder[GuildMemberRemove]            = deriveConfiguredDecoder
  implicit val guildMembersChunkDecoder: Decoder[GuildMembersChunk]            = deriveConfiguredDecoder
  implicit val guildMemberUpdateDecoder: Decoder[GuildMemberUpdate]            = deriveConfiguredDecoder
  implicit val guildRoleCreateDecoder: Decoder[GuildRoleCreate]                = deriveConfiguredDecoder
  implicit val guildRoleDeleteDecoder: Decoder[GuildRoleDelete]                = deriveConfiguredDecoder
  implicit val guildRoleUpdateDecoder: Decoder[GuildRoleUpdate]                = deriveConfiguredDecoder
  implicit val interactionCreate: Decoder[InteractionCreate]                   = deriveConfiguredDecoder
  implicit val inviteCreateDecoder: Decoder[InviteCreate]                      = deriveConfiguredDecoder
  implicit val inviteDeleteDecoder: Decoder[InviteDelete]                      = deriveConfiguredDecoder
  implicit val messageDelete: Decoder[MessageDelete]                           = deriveConfiguredDecoder
  implicit val messageDeleteBulk: Decoder[MessageDeleteBulk]                   = deriveConfiguredDecoder
  implicit val messageReactionAdd: Decoder[MessageReactionAdd]                 = deriveConfiguredDecoder
  implicit val messageReactionRemove: Decoder[MessageReactionRemove]           = deriveConfiguredDecoder
  implicit val messageReactionRemoveAll: Decoder[MessageReactionRemoveAll]     = deriveConfiguredDecoder
  implicit val messageReactionRemoveEmoji: Decoder[MessageReactionRemoveEmoji] = deriveConfiguredDecoder
  implicit val readyDecoder: Decoder[Ready]                                    = deriveConfiguredDecoder
  implicit val typingStartDecoder: Decoder[TypingStart]                        = deriveConfiguredDecoder
  implicit val voiceServerUpdateDecoder: Decoder[VoiceServerUpdate]            = deriveConfiguredDecoder
  implicit val webhookUpdateDecoder: Decoder[WebhookUpdate]                    = deriveConfiguredDecoder

  def decodeEventName(eventName: String, data: ACursor): Decoder.Result[Event] =
    eventName match {
      case "READY"                         => data.as[Ready]
      case "RESUMED"                       => Resumed.asRight
      case "CHANNEL_CREATE"                => data.as[Channel].map(ChannelCreate)
      case "CHANNEL_UPDATE"                => data.as[Channel].map(ChannelUpdate)
      case "CHANNEL_DELETE"                => data.as[Channel].map(ChannelDelete)
      case "CHANNEL_PINS_UPDATE"           => data.as[ChannelPinsUpdate]
      case "GUILD_CREATE"                  => data.as[Guild].map(GuildCreate)
      case "GUILD_UPDATE"                  => data.as[Guild].map(GuildUpdate)
      case "GUILD_DELETE"                  => data.as[Guild].map(GuildDelete)
      case "GUILD_BAN_ADD"                 => data.as[Ban].map(GuildBanAdd)
      case "GUILD_BAN_REMOVE"              => data.as[Ban].map(GuildBanRemove)
      case "GUILD_EMOJIS_UPDATE"           => data.as[GuildEmojisUpdate]
      case "GUILD_INTEGRATIONS_UPDATE"     => data.as[GuildId]
      case "GUILD_MEMBER_ADD"              => (data.get[Snowflake]("guild_id"), data.as[Member]).mapN(GuildMemberAdd)
      case "GUILD_MEMBER_REMOVE"           => data.as[GuildMemberRemove]
      case "GUILD_MEMBER_UPDATE"           => data.as[GuildMemberUpdate]
      case "GUILD_MEMBERS_CHUNK"           => data.as[GuildMembersChunk]
      case "GUILD_ROLE_CREATE"             => data.as[GuildRoleCreate]
      case "GUILD_ROLE_UPDATE"             => data.as[GuildRoleUpdate]
      case "GUILD_ROLE_DELETE"             => data.as[GuildRoleDelete]
      case "INTERACTION_CREATE"            => data.as[InteractionCreate]
      case "INVITE_CREATE"                 => data.as[InviteCreate]
      case "INVITE_DELETE"                 => data.as[InviteDelete]
      case "MESSAGE_CREATE"                => data.as[Message].map(MessageCreate)
      case "MESSAGE_UPDATE"                => data.as[Update].map(MessageUpdate)
      case "MESSAGE_DELETE"                => data.as[MessageDelete]
      case "MESSAGE_DELETE_BULK"           => data.as[MessageDeleteBulk]
      case "MESSAGE_REACTION_ADD"          => data.as[MessageReactionAdd]
      case "MESSAGE_REACTION_REMOVE"       => data.as[MessageReactionRemove]
      case "MESSAGE_REACTION_REMOVE_ALL"   => data.as[MessageReactionRemoveAll]
      case "MESSAGE_REACTION_REMOVE_EMOJI" => data.as[MessageReactionRemoveEmoji]
      case "PRESENCE_UPDATE"               => data.as[Presence].map(PresenceUpdate)
      case "TYPING_START"                  => data.as[TypingStart]
      case "USER_UPDATE"                   => data.as[User].map(UserUpdate)
      case "VOICE_STATE_UPDATE"            => data.as[VoiceState].map(VoiceStateUpdate)
      case "VOICE_SERVER_UPDATE"           => data.as[VoiceServerUpdate]
      case "WEBHOOKS_UPDATE"               => data.as[WebhookUpdate]
      case unknown                         => DecodingFailure(s"Unknown event name received: $unknown", data.history).asLeft
    }
}
