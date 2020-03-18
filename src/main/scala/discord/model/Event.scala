package discord.model

import cats.implicits._
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

sealed trait Event                       extends Product with Serializable
case class Hello(heartbeatInterval: Int) extends Event
case object HeartBeat                    extends Event
case class Ready(
    v: Integer,
    user: Json,
    sessionId: String
) extends Event

object Event {
  implicit val config: Configuration        = Configuration.default.withSnakeCaseMemberNames
  implicit val helloDecoder: Decoder[Hello] = deriveConfiguredDecoder
  implicit val readyDecoder: Decoder[Ready] = deriveConfiguredDecoder
  implicit val eventDecoder: Decoder[Event] = new Decoder[Event] {
    def apply(c: HCursor): Decoder.Result[Event] =
      for {
        op     <- c.downField("op").as[Int]
        data   = c.downField("d")
        result <- decodeOp(op, data)
      } yield result
  }

  def decodeOp(op: Int, data: ACursor): Decoder.Result[Event] = op match {
    case 10 =>
      data.as[Hello]
    case 11 =>
      HeartBeat.asRight
    case op =>
      DecodingFailure(s"Unknown op code received: $op", data.history).asLeft
  }
}
