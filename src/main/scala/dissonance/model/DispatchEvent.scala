package dissonance.model

import cats.data.NonEmptyList
import cats.implicits._
import dissonance.model.activity.Activity
import dissonance.model.user.User
import dissonance.model.guild.{Guild, GuildBan}
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import java.time.OffsetDateTime

sealed trait DispatchEvent extends Product with Serializable

object DispatchEvent {
  // TODO: Implement these types
  type ChannelType  = Json
  type ClientStatus = Json
  type Overwrite    = Json
  type Snowflake    = Long

  case class Channel(
      id: Snowflake,
      `type`: ChannelType,
      guildId: Option[Snowflake],
      position: Int,
      permissionOverwrites: List[Overwrite],
      name: String,
      topic: Option[String],
      nsfw: Option[Boolean],
      lastMessageId: Option[Snowflake],
      bitrate: Option[Int],
      userLimit: Option[Int],
      rateLimitPerUser: Option[Int],
      recipients: Option[NonEmptyList[User]],
      icon: Option[String],
      ownerId: Option[Snowflake],
      applicationId: Option[Snowflake],
      parentId: Option[Snowflake],
      lastPinTimestamp: Option[OffsetDateTime]
  ) extends DispatchEvent

  case class ChannelPinsUpdate(guildId: Snowflake, channelId: Snowflake, lastPinTimestamp: OffsetDateTime) extends DispatchEvent

  case class GuildCreate(guild: Guild) extends DispatchEvent

  case class GuildUpdate(guild: Guild) extends DispatchEvent

  case class GuildBanAdd(guildBan: GuildBan) extends DispatchEvent

  case class GuildBanRemove(guildBan: GuildBan) extends DispatchEvent

  case class GuildEmojis(guildId: Snowflake, emojis: List[Emoji]) extends DispatchEvent

  case class GuildId(guildId: Snowflake) extends DispatchEvent

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
      premiumSince: Option[OffsetDateTime],
      nick: Option[String]
  ) extends DispatchEvent

  case class ReactionAdd(json: Json) extends DispatchEvent

  case class Ready(
      v: Integer,
      user: User,
      sessionId: String,
      shard: Option[(Int, Int)] // TODO: Parse the 2 element array into a case class instead of tuple
  ) extends DispatchEvent

  case object Resumed extends DispatchEvent

  case class TypingStart(json: Json) extends DispatchEvent

  def ImplementMe(name: String) = DecodingFailure(s"UNIMPLEMENTED: $name", Nil).asLeft

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

  implicit val channelDecoder: Decoder[Channel]                     = deriveConfiguredDecoder
  implicit val channelPinsUpdateDecoder: Decoder[ChannelPinsUpdate] = deriveConfiguredDecoder
  implicit val guildEmojisDecoder: Decoder[GuildEmojis]             = deriveConfiguredDecoder
  implicit val guildIdDecoder: Decoder[GuildId]                     = deriveConfiguredDecoder
  implicit val presenceUpdateDecoder: Decoder[PresenceUpdate]       = deriveConfiguredDecoder
  implicit val readyDecoder: Decoder[Ready]                         = deriveConfiguredDecoder

  // TODO: Finish implementing all the events:
  // https://discordapp.com/developers/docs/topics/gateway#commands-and-events-gateway-events
  def decodeEventName(eventName: String, data: ACursor): Decoder.Result[DispatchEvent] = eventName match {
    case "READY" =>
      data.as[Ready]
    case "RESUMED" =>
      Resumed.asRight
    // TODO: We lose context here. CHANNEL_CREATE, CHANNEL_UPDATE, and CHANNEL_DELETE provide a
    // TODO: Channel object but we don't know if it was created or updated or deleted anymore
    // TODO: We can do this just like we did with MessageCreate/MessageDelete and GuildCreate/GuildUpdate
    case "CHANNEL_CREATE" =>
      data.as[Channel]
    case "CHANNEL_UPDATE" =>
      data.as[Channel]
    case "CHANNEL_DELETE" =>
      data.as[Channel]
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
      data.as[GuildBan].map(GuildBanAdd)
    case "GUILD_BAN_REMOVE" =>
      data.as[GuildBan].map(GuildBanRemove)
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
