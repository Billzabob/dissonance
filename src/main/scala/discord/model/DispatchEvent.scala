package discord.model

import cats.implicits._
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

sealed trait DispatchEvent extends Product with Serializable

object DispatchEvent {
  case class Ready(
      v: Integer,
      user: Json,
      sessionId: String,
      shard: Option[(Int, Int)]
  ) extends DispatchEvent
  case object Resumed                        extends DispatchEvent
  case class GuildCreate(json: Json)         extends DispatchEvent
  case class GuildMemberUpdate(json: Json)   extends DispatchEvent
  case class MessageCreate(message: Message) extends DispatchEvent
  case class ReactionAdd(json: Json)         extends DispatchEvent
  case class PresenceUpdate(json: Json)      extends DispatchEvent
  case class TypingStart(json: Json)         extends DispatchEvent

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

  implicit val readyDecoder: Decoder[Ready] = deriveConfiguredDecoder

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
      data.as[Json].map(GuildCreate)
    case n @ "GUILD_UPDATE" =>
      ImplementMe(n)
    case n @ "GUILD_DELETE" =>
      ImplementMe(n)
    case n @ "GUILD_BAN_ADD" =>
      ImplementMe(n)
    case n @ "GUILD_BAN_REMOVE" =>
      ImplementMe(n)
    case n @ "GUILD_EMOJIS_UPDATE" =>
      ImplementMe(n)
    case n @ "GUILD_INTEGRATIONS_UPDATE" =>
      ImplementMe(n)
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
      data.as[Json].map(PresenceUpdate)
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
