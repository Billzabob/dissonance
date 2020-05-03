package dissonance.model

import cats.implicits._
import dissonance.model._
import dissonance.model.channel.Channel
import dissonance.model.guild.Guild
import dissonance.model.user.User
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import java.time.OffsetDateTime
import java.time.Instant

sealed trait Event extends Product with Serializable

object Event {
  // TODO: Implement
  type Snowflake = Long

  case class ChannelCreate(channel: Channel) extends Event
  case class ChannelDelete(channel: Channel) extends Event
  case class ChannelPinsUpdate(
      guildId: Snowflake,
      channelId: Snowflake,
      lastPinTimestamp: OffsetDateTime
  ) extends Event
  case class ChannelUpdate(channel: Channel)                      extends Event
  case class GuildBanAdd(guildBan: guild.Ban)                     extends Event
  case class GuildBanRemove(guildBan: guild.Ban)                  extends Event
  case class GuildCreate(guild: Guild)                            extends Event
  case class GuildEmojis(guildId: Snowflake, emojis: List[Emoji]) extends Event
  case class GuildId(guildId: Snowflake)                          extends Event
  case class GuildMemberUpdate(json: Json)                        extends Event
  case class GuildUpdate(guild: Guild)                            extends Event
  case class MessageCreate(message: Message)                      extends Event
  case class PresenceUpdate(presence: Presence)                   extends Event
  case class MessageReactionAdd(
      userId: Snowflake,
      channelId: Snowflake,
      messageId: Snowflake,
      guildId: Option[Snowflake],
      member: Option[guild.Member],
      emoji: Emoji
  ) extends Event
  case class Ready(
      v: Integer,
      user: User,
      sessionId: String,
      shard: Option[(Int, Int)]
  ) extends Event // TODO: Parse the 2 element array into a case class instead of tuple
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
  implicit val messageReactionAdd: Decoder[MessageReactionAdd]      = deriveConfiguredDecoder
  implicit val presenceUpdateDecoder: Decoder[PresenceUpdate]       = deriveConfiguredDecoder
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
