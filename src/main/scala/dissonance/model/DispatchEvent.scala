package dissonance.model

import cats.implicits._
import dissonance.model.user.User
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import java.time.Instant
import org.http4s.circe._
import org.http4s.Uri

// TODO: This sealed trait is going to have a TON of members that all need to be in this file, making it huge.
// TODO: Is there a better way?
sealed trait DispatchEvent extends Product with Serializable

object DispatchEvent {

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

  // TODO: Implement these types
  type ActivityFlags = Unit
  type Assets        = Unit
  type ChannelType   = Unit
  type ClientStatus  = Unit
  type GuildFeature  = String
  type GuildMember   = Unit
  type Overwrite     = Unit
  type Party         = Unit
  type Role          = Unit
  type Secrets       = Unit
  type Snowflake     = Long
  type VoiceState    = Unit

  // TODO: Move case classes that don't extend DispatchEvent out of here since this file is already gonna be huge
  case class Activity(
      name: String,
      `type`: ActivityType,
      url: Option[Uri],
      createdAt: Timestamp,
      timestamps: Timestamps,
      applicationId: Snowflake,
      details: Option[String],
      state: Option[String],
      emoji: Option[Emoji],
      party: Party,
      assets: Assets,
      secrets: Secrets,
      instance: Boolean,
      flags: ActivityFlags
  )

  object Activity {
    implicit val activityDecoder: Decoder[Activity] = deriveConfiguredDecoder
  }

  sealed trait ActivityType extends Product with Serializable

  object ActivityType {
    case object Game      extends ActivityType
    case object Streaming extends ActivityType
    case object Listening extends ActivityType
    case object Custom    extends ActivityType

    implicit val activityTypeDecoder: Decoder[ActivityType] = Decoder[Int].emap {
      case 0     => Right(Game)
      case 1     => Right(Streaming)
      case 2     => Right(Listening)
      case 4     => Right(Custom)
      case other => Left(s"Unknown activity type ID: $other")
    }
  }

  case class Timestamps(start: Timestamp, end: Timestamp)

  object Timestamps {
    implicit val timestampsDecoder: Decoder[Timestamps] = deriveConfiguredDecoder
  }

  case class Timestamp(time: Instant)

  object Timestamp {
    // Decodes either an ISO8601 timestamp string or a unix epoch number, since Discord uses both in different places
    // Note: If we need to have an Encoder this won't work since we won't know whether to turn this into a timestamp or an epoch number
    implicit val timestampDecoder: Decoder[Timestamp] =
      (Decoder[Instant] or Decoder[Long].map(Instant.ofEpochMilli)).map(Timestamp.apply)
  }

  case class Emoji(name: String, id: Snowflake, animated: Boolean)

  object Emoji {
    implicit val emojiDecoder: Decoder[Emoji] = deriveConfiguredDecoder
  }

  case class Channel(
      id: Snowflake,
      `type`: ChannelType,
      guildId: Snowflake,
      position: Int,
      permissionOverwrites: List[Overwrite],
      name: String,
      topic: Option[String],
      nsfw: Boolean,
      lastMessageId: Option[Snowflake],
      bitrate: Int,
      userLimit: Int,
      rateLimitPerUser: Int,
      recipients: List[User],
      icon: Option[String],
      ownerId: Snowflake,
      applicationId: Snowflake,
      parentId: Option[Snowflake],
      lastPinTimestamp: Timestamp
  ) extends DispatchEvent

  object Channel {
    implicit val channelDecoder: Decoder[Channel] = deriveConfiguredDecoder
  }

  case class ChannelPinsUpdate(guildId: Snowflake, channelId: Snowflake, lastPinTimestamp: Timestamp) extends DispatchEvent

  object ChannelPinsUpdate {
    implicit val channelPinsUpdateDecoder: Decoder[ChannelPinsUpdate] = deriveConfiguredDecoder
  }

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

  object Guild {
    implicit val guildDecoder: Decoder[Guild] = deriveConfiguredDecoder
  }

  case class GuildBan(guildId: Snowflake, user: User) extends DispatchEvent

  object GuildBan {
    implicit val guildBanDecoder: Decoder[GuildBan] = deriveConfiguredDecoder
  }

  case class GuildEmojis(guildId: Snowflake, emojis: List[Emoji]) extends DispatchEvent

  object GuildEmojis {
    implicit val guildEmojisDecoder: Decoder[GuildEmojis] = deriveConfiguredDecoder
  }

  case class GuildId(guildId: Snowflake) extends DispatchEvent

  object GuildId {
    implicit val guildIdDecoder: Decoder[GuildId] = deriveConfiguredDecoder
  }

  case class GuildMemberUpdate(json: Json) extends DispatchEvent

  case class MessageCreate(message: Message) extends DispatchEvent

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

  object PresenceUpdate {
    implicit val presenceUpdateDecoder: Decoder[PresenceUpdate] = deriveConfiguredDecoder
  }

  case class ReactionAdd(json: Json) extends DispatchEvent

  case class Ready(
      v: Integer,
      user: User,
      sessionId: String,
      shard: Option[(Int, Int)]
  ) extends DispatchEvent

  object Ready {
    implicit val readyDecoder: Decoder[Ready] = deriveConfiguredDecoder
  }

  case object Resumed extends DispatchEvent

  case class TypingStart(json: Json) extends DispatchEvent

  def ImplementMe(name: String) = DecodingFailure(s"UNIMPLEMENTED: $name", Nil).asLeft

  // TODO: Finish implementing all the events:
  // https://discordapp.com/developers/docs/topics/gateway#commands-and-events-gateway-events
  def decodeEventName(eventName: String, data: ACursor): Decoder.Result[DispatchEvent] = eventName match {
    case "READY" =>
      data.as[Ready]
    case "RESUMED" =>
      Resumed.asRight
    // TODO: We lose context here. CHANNEL_CREATE, CHANNEL_UPDATE, and CHANNEL_DELETE provide a
    // TODO: Channel object but we don't know if it was created or updated or deleted anymore
    case "CHANNEL_CREATE" =>
      data.as[Channel]
    case "CHANNEL_UPDATE" =>
      data.as[Channel]
    case "CHANNEL_DELETE" =>
      data.as[Channel]
    case "CHANNEL_PINS_UPDATE" =>
      data.as[ChannelPinsUpdate]
    case "GUILD_CREATE" =>
      data.as[Guild]
    case "GUILD_UPDATE" =>
      data.as[Guild]
    case n @ "GUILD_DELETE" =>
      // Not sure on this one, since the docs give an example but no schema
      ImplementMe(n)
    case "GUILD_BAN_ADD" =>
      data.as[GuildBan]
    case "GUILD_BAN_REMOVE" =>
      data.as[GuildBan]
    case "GUILD_EMOJIS_UPDATE" =>
      data.as[GuildEmojis]
    case "GUILD_INTEGRATIONS_UPDATE" =>
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
