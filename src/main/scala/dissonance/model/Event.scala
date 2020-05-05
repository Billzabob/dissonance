package dissonance.model

import cats.implicits._
import dissonance.model._
import dissonance.model.channel.Channel
import dissonance.model.guild.Guild
import dissonance.model.message.Message
import dissonance.model.user.User
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import java.time.OffsetDateTime
import java.time.Instant

sealed trait Event extends Product with Serializable

object Event {
  case class ChannelCreate(channel: Channel) extends Event
  case class ChannelDelete(channel: Channel) extends Event
  case class ChannelPinsUpdate(
      guildId: Snowflake,
      channelId: Snowflake,
      lastPinTimestamp: OffsetDateTime
  ) extends Event
  case class ChannelUpdate(channel: Channel)                          extends Event
  case class GuildBanAdd(guildBan: guild.Ban)                         extends Event
  case class GuildBanRemove(guildBan: guild.Ban)                      extends Event
  case class GuildCreate(guild: Guild)                                extends Event
  case class GuildEmojis(guildId: Snowflake, emojis: List[Emoji])     extends Event
  case class GuildId(guildId: Snowflake)                              extends Event
  case class GuildMemberAdd(guildId: Snowflake, member: guild.Member) extends Event
  case class GuildMembersChunk(
      guildId: Snowflake,
      members: List[guild.Member],
      chunkIndex: Int,
      chunkCount: Int,
      notFound: Option[List[Json]],
      presences: Option[List[Presence]],
      nonce: Option[String]
  ) extends Event
  case class GuildMemberRemove(guildId: Snowflake, user: User) extends Event
  case class GuildMemberUpdate(
      guildId: Snowflake,
      roles: List[Snowflake],
      user: User,
      nick: Option[String],
      premiumSince: Option[OffsetDateTime]
  ) extends Event
  case class GuildRoleCreate(guildId: Snowflake, role: guild.Role)  extends Event
  case class GuildRoleDelete(guildId: Snowflake, roleId: Snowflake) extends Event
  case class GuildRoleUpdate(guildId: Snowflake, role: guild.Role)  extends Event
  case class GuildUpdate(guild: Guild)                              extends Event
  case class InviteCreate(
      channelId: Snowflake,
      code: String,
      createdAt: Instant,
      guildId: Option[Snowflake],
      inviter: Option[User],
      maxAge: Int, // TODO: This is in seconds, so convert to Duration?
      maxUses: Int,
      targetUser: Option[User],
      targetUserType: Option[user.TargetUserType],
      temporary: Boolean,
      uses: Int
  ) extends Event
  case class InviteDelete(channelId: Snowflake, guildId: Option[Snowflake], code: String)              extends Event
  case class MessageCreate(message: Message)                                                           extends Event
  case class MessageDelete(id: Snowflake, channelId: Snowflake, guildId: Option[Snowflake])            extends Event
  case class MessageDeleteBulk(ids: List[Snowflake], channelId: Snowflake, guildId: Option[Snowflake]) extends Event
  case class MessageReactionAdd(
      userId: Snowflake,
      channelId: Snowflake,
      messageId: Snowflake,
      guildId: Option[Snowflake],
      member: Option[guild.Member],
      emoji: Emoji
  ) extends Event
  case class MessageUpdate(message: Message)    extends Event // TODO: Probably won't work since inner message for Update might only have id and channelId.
  case class PresenceUpdate(presence: Presence) extends Event
  case class Ready(
      v: Integer,
      user: User,
      sessionId: String,
      shard: Option[(Int, Int)] // TODO: Parse the 2 element array into a case class instead of tuple
  ) extends Event
  case object Resumed extends Event
  case class TypingStart(
      channelId: Snowflake,
      guildId: Option[Snowflake],
      userId: Snowflake,
      timestamp: Instant,
      member: Option[guild.Member]
  ) extends Event

  def ImplementMe(name: String) = DecodingFailure(s"UNIMPLEMENTED: $name", Nil).asLeft

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

  implicit val channelPinsUpdateDecoder: Decoder[ChannelPinsUpdate] = deriveConfiguredDecoder
  implicit val guildEmojisDecoder: Decoder[GuildEmojis]             = deriveConfiguredDecoder
  implicit val guildIdDecoder: Decoder[GuildId]                     = deriveConfiguredDecoder
  implicit val guildMemberRemoveDecoder: Decoder[GuildMemberRemove] = deriveConfiguredDecoder
  implicit val guildMembersChunkDecoder: Decoder[GuildMembersChunk] = deriveConfiguredDecoder
  implicit val guildMemberUpdateDecoder: Decoder[GuildMemberUpdate] = deriveConfiguredDecoder
  implicit val guildRoleCreateDecoder: Decoder[GuildRoleCreate]     = deriveConfiguredDecoder
  implicit val guildRoleDeleteDecoder: Decoder[GuildRoleDelete]     = deriveConfiguredDecoder
  implicit val guildRoleUpdateDecoder: Decoder[GuildRoleUpdate]     = deriveConfiguredDecoder
  implicit val inviteCreateDecoder: Decoder[InviteCreate]           = deriveConfiguredDecoder
  implicit val inviteDeleteDecoder: Decoder[InviteDelete]           = deriveConfiguredDecoder
  implicit val messageDelete: Decoder[MessageDelete]                = deriveConfiguredDecoder
  implicit val messageDeleteBulk: Decoder[MessageDeleteBulk]        = deriveConfiguredDecoder
  implicit val messageReactionAdd: Decoder[MessageReactionAdd]      = deriveConfiguredDecoder
  implicit val readyDecoder: Decoder[Ready]                         = deriveConfiguredDecoder
  implicit val typingStartDecoder: Decoder[TypingStart]             = deriveConfiguredDecoder

  // TODO: Finish implementing all the events:
  // https://discordapp.com/developers/docs/topics/gateway#commands-and-events-gateway-events
  def decodeEventName(eventName: String, data: ACursor): Decoder.Result[Event] = eventName match {
    case "READY" =>
      data.as[Ready]
    case "RESUMED" =>
      Resumed.asRight
    case "CHANNEL_CREATE" =>
      data.as[Channel].map(ChannelCreate)
    case "CHANNEL_UPDATE" =>
      data.as[Channel].map(ChannelUpdate)
    case "CHANNEL_DELETE" =>
      data.as[Channel].map(ChannelDelete)
    case "CHANNEL_PINS_UPDATE" =>
      data.as[ChannelPinsUpdate]
    case "GUILD_CREATE" =>
      data.as[Guild].map(GuildCreate)
    case "GUILD_UPDATE" =>
      data.as[Guild].map(GuildUpdate)
    case n @ "GUILD_DELETE" =>
      // Not sure on this one, since the docs give an example but no schema
      ImplementMe(n)
    case "GUILD_BAN_ADD" =>
      data.as[guild.Ban].map(GuildBanAdd)
    case "GUILD_BAN_REMOVE" =>
      data.as[guild.Ban].map(GuildBanRemove)
    case "GUILD_EMOJIS_UPDATE" =>
      data.as[GuildEmojis]
    case "GUILD_INTEGRATIONS_UPDATE" =>
      data.as[GuildId]
    case "GUILD_MEMBER_ADD" =>
      (data.get[Snowflake]("guild_id"), data.as[guild.Member]).mapN(GuildMemberAdd)
    case "GUILD_MEMBER_REMOVE" =>
      data.as[GuildMemberRemove]
    case "GUILD_MEMBER_UPDATE" =>
      data.as[GuildMemberUpdate]
    case "GUILD_MEMBERS_CHUNK" =>
      data.as[GuildMembersChunk]
    case "GUILD_ROLE_CREATE" =>
      data.as[GuildRoleCreate]
    case "GUILD_ROLE_UPDATE" =>
      data.as[GuildRoleUpdate]
    case "GUILD_ROLE_DELETE" =>
      data.as[GuildRoleDelete]
    case "INVITE_CREATE" =>
      data.as[InviteCreate]
    case "INVITE_DELETE" =>
      data.as[InviteDelete]
    case "MESSAGE_CREATE" =>
      data.as[Message].map(MessageCreate)
    case "MESSAGE_UPDATE" =>
      data.as[Message].map(MessageUpdate)
    case "MESSAGE_DELETE" =>
      data.as[MessageDelete]
    case "MESSAGE_DELETE_BULK" =>
      data.as[MessageDeleteBulk]
    case "MESSAGE_REACTION_ADD" =>
      data.as[MessageReactionAdd]
    case n @ "MESSAGE_REACTION_REMOVE" =>
      ImplementMe(n)
    case n @ "MESSAGE_REACTION_REMOVE_ALL" =>
      ImplementMe(n)
    case n @ "MESSAGE_REACTION_REMOVE_EMOJI" =>
      ImplementMe(n)
    case "PRESENCE_UPDATE" =>
      data.as[Presence].map(PresenceUpdate)
    case "TYPING_START" =>
      data.as[TypingStart]
    case n @ "USER_UPDATE" =>
      ImplementMe(n)
    case n @ "VOICE_STATE_UPDATE" =>
      ImplementMe(n)
    case n @ "VOICE_SERVER_UPDATE" =>
      ImplementMe(n)
    case n @ "WEBHOOKS_UPDATE" =>
      ImplementMe(n)
    case unknown =>
      DecodingFailure(s"Unknown event name received: $unknown", data.history).asLeft
  }
}
