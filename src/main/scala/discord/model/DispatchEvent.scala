package discord.model

import cats.implicits._
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

// TODO: This sealed trait is going to have a TON of members that all need to be in this file, making it huge.
// TODO: Is there a better way?
sealed trait DispatchEvent extends Product with Serializable

object DispatchEvent {
  case class Ready(
      v: Integer,
      user: Json,
      sessionId: String,
      shard: Option[(Int, Int)]
  ) extends DispatchEvent
  case object Resumed                        extends DispatchEvent
  case class GuildMemberUpdate(json: Json)   extends DispatchEvent
  case class MessageCreate(message: Message) extends DispatchEvent
  case class ReactionAdd(json: Json)         extends DispatchEvent
  case class TypingStart(json: Json)         extends DispatchEvent

  // TODO: Better typing
  type Snowflake    = Long
  type Role         = Unit
  type Emoji        = Unit
  type GuildFeature = String
  type Timestamp    = String
  type VoiceState   = Unit
  type GuildMember  = Unit
  type Channel      = Unit
  type Activity     = Unit
  type ClientStatus = Unit

  case class PresenceUpdate(
      user: User,
      roles: List[Snowflake],
      game: Option[Activity],
      guildId: Snowflake,
      status: String,
      activities: List[Activity],
      clientStatus: ClientStatus,
      premiumSince: Option[Timestamp],
      nick: Option[String]
  ) extends DispatchEvent

  case class Guild(
      id: Snowflake,
      name: String,
      icon: Option[String],
      splash: Option[String],
      discoverySplash: Option[String],
      owner: Boolean,
      ownerId: Snowflake,
      permissions: Int,
      region: String,
      afkChannelId: Option[Snowflake],
      afkTimeout: Integer,
      embedEnabled: Boolean,
      embedChannelId: Option[Snowflake],
      verificationLevel: Int,
      defaultMessageNotifications: Int,
      explicitContentFilter: Int,
      roles: List[Role],
      emojis: List[Emoji],
      features: List[GuildFeature],
      mfaLevel: Int,
      applicationId: Option[Snowflake],
      widgetEnabled: Boolean,
      widgetChannelId: Option[Snowflake],
      systemChannelId: Option[Snowflake],
      systemChannelFlags: Int,
      rulesChannelId: Option[Snowflake],
      joinedAt: Timestamp,
      large: Boolean,
      unavailable: Boolean,
      memberCount: Int,
      voiceStates: List[VoiceState],
      members: List[GuildMember],
      channels: List[Channel],
      presences: List[PresenceUpdate],
      maxPresences: Option[Int],
      maxMembers: Int,
      vanityUrlCode: Option[String],
      description: Option[String],
      banner: Option[String],
      premiumTier: Int,
      premiumSubscriptionCount: Integer,
      preferredLocal: String,
      publicUpdatesChannelId: Option[Snowflake],
      approximateMemberCount: Int,
      approximatePresenceCount: Int
  ) extends DispatchEvent

  case class GuildBan(guildId: Snowflake, user: User) extends DispatchEvent

  case class GuildEmojis(guildId: Snowflake, emojis: List[Emoji]) extends DispatchEvent

  case class GuildId(guildId: Snowflake) extends DispatchEvent

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

  implicit val readyDecoder: Decoder[Ready]                   = deriveConfiguredDecoder
  implicit val presenceUpdateDecoder: Decoder[PresenceUpdate] = deriveConfiguredDecoder
  implicit val guildDecoder: Decoder[Guild]                   = deriveConfiguredDecoder
  implicit val guildBanDecoder: Decoder[GuildBan]             = deriveConfiguredDecoder
  implicit val guildEmojisDecoder: Decoder[GuildEmojis]       = deriveConfiguredDecoder
  implicit val guildIdDecoder: Decoder[GuildId]               = deriveConfiguredDecoder

  def ImplementMe(name: String) = DecodingFailure(s"UNIMPLEMENTED: $name", Nil).asLeft

  // TODO: Finish implementing all the events:
  // https://discordapp.com/developers/docs/topics/gateway#commands-and-events-gateway-events
  def decodeEventName(eventName: String, data: ACursor): Decoder.Result[DispatchEvent] = eventName match {
    case "READY" =>
      data.as[Ready]
    case "RESUMED" =>
      Resumed.asRight
    case n @ "CHANNEL_CREATE" =>
      ImplementMe(n)
    case n @ "CHANNEL_UPDATE" =>
      ImplementMe(n)
    case n @ "CHANNEL_DELETE" =>
      ImplementMe(n)
    case n @ "CHANNEL_PINS_UPDATE" =>
      ImplementMe(n)
    case "GUILD_CREATE" =>
      data.as[Guild]
    case n @ "GUILD_UPDATE" =>
      data.as[Guild]
    case n @ "GUILD_DELETE" =>
      ImplementMe(n)
    // TODO: We lose context here. Both GUILD_BAN_ADD and GUILD_BAN_REMOVE provide a
    // TODO: GuildBan object but we don't know if it was added or removed anymore
    case n @ "GUILD_BAN_ADD" =>
      data.as[GuildBan]
    case n @ "GUILD_BAN_REMOVE" =>
      data.as[GuildBan]
    case n @ "GUILD_EMOJIS_UPDATE" =>
      data.as[GuildEmojis]
    case n @ "GUILD_INTEGRATIONS_UPDATE" =>
      data.as[GuildId]
    case n @ "GUILD_MEMBER_ADD" =>
      ImplementMe(n)
    case n @ "GUILD_MEMBER_REMOVE" =>
      ImplementMe(n)
    case "GUILD_MEMBER_UPDATE" =>
      data.as[Json].map(GuildMemberUpdate)
    case n @ "GUILD_MEMBERS_CHUNK" =>
      ImplementMe(n)
    case n @ "GUILD_ROLE_CREATE" =>
      ImplementMe(n)
    case n @ "GUILD_ROLE_UPDATE" =>
      ImplementMe(n)
    case n @ "GUILD_ROLE_DELETE" =>
      ImplementMe(n)
    case n @ "INVITE_CREATE" =>
      ImplementMe(n)
    case n @ "INVITE_DELETE" =>
      ImplementMe(n)
    case "MESSAGE_CREATE" =>
      data.as[Message].map(MessageCreate)
    case n @ "MESSAGE_UPDATE" =>
      ImplementMe(n)
    case n @ "MESSAGE_DELETE" =>
      ImplementMe(n)
    case n @ "MESSAGE_DELETE_BULK" =>
      ImplementMe(n)
    case "MESSAGE_REACTION_ADD" =>
      data.as[Json].map(ReactionAdd)
    case n @ "MESSAGE_REACTION_REMOVE" =>
      ImplementMe(n)
    case n @ "MESSAGE_REACTION_REMOVE_ALL" =>
      ImplementMe(n)
    case n @ "MESSAGE_REACTION_REMOVE_EMOJI" =>
      ImplementMe(n)
    case "PRESENCE_UPDATE" =>
      data.as[PresenceUpdate]
    case "TYPING_START" =>
      data.as[Json].map(TypingStart)
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
