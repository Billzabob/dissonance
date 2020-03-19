package discord.model

import cats.implicits._
import discord.utils.json._
import io.circe._
import io.circe.generic.extras.semiauto._
import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration

sealed trait Event                                  extends Product with Serializable
case object HeartBeatAck                            extends Event
case class Hello(heartbeatInterval: FiniteDuration) extends Event
case class Heartbeat(d: Option[Int])                extends Event
case class Dispatch(s: Int, d: DispatchEvent)       extends Event

object Hello {
  implicit val helloDecoder: Decoder[Hello] = deriveConfiguredDecoder
}

object Heartbeat {
  implicit val heartbeatEncoder: Encoder[Heartbeat] = deriveConfiguredEncoder[Heartbeat].mapJsonObject(_.add("op", Json.fromInt(1)))
  implicit val heartbeatDecoder: Decoder[Heartbeat] = deriveConfiguredDecoder
}

sealed trait DispatchEvent
case class Ready(
    v: Integer,
    user: Json,
    sessionId: String
) extends DispatchEvent
case class GuildCreate(json: Json)    extends DispatchEvent
case class MessageCreate(json: Json)  extends DispatchEvent
case class TypingStart(json: Json)    extends DispatchEvent
case class ReactionAdd(json: Json)    extends DispatchEvent
case class PresenceUpdate(json: Json) extends DispatchEvent

object Event {
  implicit val eventDecoder: Decoder[Event] = cursor =>
    for {
      op     <- cursor.get[Int]("op")
      result <- decodeOp(op, cursor)
    } yield result

  def decodeOp(op: Int, data: HCursor): Decoder.Result[Event] = op match {
    case 0 =>
      data.as[Dispatch]
    case 1 =>
      data.get[Heartbeat]("d")
    case 10 =>
      data.get[Hello]("d")
    case 11 =>
      HeartBeatAck.asRight
    case unknown =>
      DecodingFailure(s"Unknown op code received: $unknown", data.history).asLeft
  }
}

object Dispatch {
  implicit val readyDecoder: Decoder[Ready] = deriveConfiguredDecoder
  implicit val eventDecoder: Decoder[Dispatch] = cursor =>
    for {
      eventName <- cursor.get[String]("t") // TODO: How to use decodeAccumulating?
      s         <- cursor.get[Int]("s")
      result    <- decodeEventName(cursor.downField("d"), eventName).map(Dispatch(s, _))
    } yield result

  def decodeEventName(data: ACursor, eventName: String) = eventName match {
    case "READY" =>
      data.as[Ready]
    case "GUILD_CREATE" =>
      data.as[Json].map(GuildCreate)
    case "MESSAGE_CREATE" =>
      data.as[Json].map(MessageCreate)
    case "TYPING_START" =>
      data.as[Json].map(TypingStart)
    case "MESSAGE_REACTION_ADD" =>
      data.as[Json].map(ReactionAdd)
    case "PRESENCE_UPDATE" =>
      data.as[Json].map(PresenceUpdate)
    case unknown =>
      DecodingFailure(s"Unknown event name received: $unknown", data.history).asLeft
  }
}
