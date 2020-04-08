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

  val ImplementMe = DecodingFailure("UNIMPLEMENTED", Nil).asLeft

  def decodeEventName(eventName: String, data: ACursor): Decoder.Result[DispatchEvent] = eventName match {
    case "READY" =>
      data.as[Ready]
    case "RESUMED" =>
      Resumed.asRight
    case "CHANNEL_CREATE" =>
      ImplementMe
    case "CHANNEL_UPDATE" =>
      ImplementMe
    case "CHANNEL_DELETE" =>
      ImplementMe
    case "CHANNEL_PINS_UPDATE" =>
      ImplementMe
    case "GUILD_CREATE" =>
      data.as[Json].map(GuildCreate)
    case "GUILD_UPDATE" =>
      ImplementMe
    case "GUILD_DELETE" =>
      ImplementMe
    case "GUILD_BAN_ADD" =>
      ImplementMe
    case "GUILD_BAN_REMOVE" =>
      ImplementMe
    case "GUILD_EMOJIS_UPDATE" =>
      ImplementMe
    case "GUILD_INTEGRATIONS_UPDATE" =>
      ImplementMe
    case "GUILD_MEMBER_ADD" =>
      ImplementMe
    case "GUILD_MEMBER_REMOVE" =>
      ImplementMe
    case "GUILD_MEMBER_UPDATE" =>
      data.as[Json].map(GuildMemberUpdate)
    case "GUILD_MEMBERS_CHUNK" =>
      ImplementMe
    case "GUILD_ROLE_CREATE" =>
      ImplementMe
    case "GUILD_ROLE_UPDATE" =>
      ImplementMe
    case "GUILD_ROLE_DELETE" =>
      ImplementMe
    case "INVITE_CREATE" =>
      ImplementMe
    case "INVITE_DELETE" =>
      ImplementMe
    case "MESSAGE_CREATE" =>
      data.as[Message].map(MessageCreate)
    case "MESSAGE_UPDATE" =>
      ImplementMe
    case "MESSAGE_DELETE" =>
      ImplementMe
    case "MESSAGE_DELETE_BULK" =>
      ImplementMe
    case "MESSAGE_REACTION_ADD" =>
      data.as[Json].map(ReactionAdd)
    case "MESSAGE_REACTION_REMOVE" =>
      ImplementMe
    case "MESSAGE_REACTION_REMOVE_ALL" =>
      ImplementMe
    case "MESSAGE_REACTION_REMOVE_EMOJI" =>
      ImplementMe
    case "PRESENCE_UPDATE" =>
      data.as[Json].map(PresenceUpdate)
    case "TYPING_START" =>
      data.as[Json].map(TypingStart)
    case "USER_UPDATE" =>
      ImplementMe
    case "VOICE_STATE_UPDATE" =>
      ImplementMe
    case "VOICE_SERVER_UPDATE" =>
      ImplementMe
    case "WEBHOOKS_UPDATE" =>
      ImplementMe
    case unknown =>
      DecodingFailure(s"Unknown event name received: $unknown", data.history).asLeft
  }
}
