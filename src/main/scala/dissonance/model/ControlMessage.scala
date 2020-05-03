package dissonance.model

import cats.implicits._
import dissonance.model.Event
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration

sealed trait ControlMessage extends Product with Serializable

object ControlMessage {
  case class Dispatch(s: Int, d: Event)               extends ControlMessage
  case class Heartbeat(d: Option[Int])                extends ControlMessage
  case object Reconnect                               extends ControlMessage
  case class InvalidSession(d: Boolean)               extends ControlMessage
  case class Hello(heartbeatInterval: FiniteDuration) extends ControlMessage
  case object HeartBeatAck                            extends ControlMessage

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

  implicit val heartbeatEncoder: Encoder[Heartbeat]           = deriveConfiguredEncoder[Heartbeat].mapJsonObject(_.add("op", Json.fromInt(1)))
  implicit val heartbeatDecoder: Decoder[Heartbeat]           = deriveConfiguredDecoder
  implicit val invalidSessionDecoder: Decoder[InvalidSession] = deriveConfiguredDecoder
  implicit val helloDecoder: Decoder[Hello]                   = Decoder[Int].at("heartbeat_interval").map(_.milliseconds).map(Hello)

  implicit val dispatchDecoder: Decoder[Dispatch] = cursor =>
    for {
      eventName      <- cursor.get[String]("t") // TODO: How to use decodeAccumulating?
      sequenceNumber <- cursor.get[Int]("s")
      event          <- Event.decodeEventName(eventName, cursor.downField("d"))
    } yield Dispatch(sequenceNumber, event)

  implicit val eventDecoder: Decoder[ControlMessage] = cursor =>
    for {
      op     <- cursor.get[Int]("op")
      result <- decodeOp(op, cursor)
    } yield result

  def decodeOp(op: Int, data: HCursor): Decoder.Result[ControlMessage] = op match {
    case 0 =>
      data.as[Dispatch]
    case 1 =>
      data.get[Heartbeat]("d")
    case 7 =>
      Reconnect.asRight
    case 9 =>
      data.as[InvalidSession]
    case 10 =>
      data.get[Hello]("d")
    case 11 =>
      HeartBeatAck.asRight
    case unknown =>
      DecodingFailure(s"Unknown op code received: $unknown", data.history).asLeft
  }
}
