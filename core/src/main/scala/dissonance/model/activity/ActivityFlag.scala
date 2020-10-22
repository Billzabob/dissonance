package dissonance.model.activity

import dissonance.model.BitFlag
import enumeratum._
import io.circe.{Decoder, Encoder}

sealed trait ActivityFlag extends EnumEntry with BitFlag with Product with Serializable

object ActivityFlag extends Enum[ActivityFlag] {
  case object Instance    extends ActivityFlag { val mask = 1 << 0 }
  case object Join        extends ActivityFlag { val mask = 1 << 1 }
  case object Spectate    extends ActivityFlag { val mask = 1 << 2 }
  case object JoinRequest extends ActivityFlag { val mask = 1 << 3 }
  case object Sync        extends ActivityFlag { val mask = 1 << 4 }
  case object Play        extends ActivityFlag { val mask = 1 << 5 }

  val values = findValues

  implicit val decoder: Decoder[List[ActivityFlag]] = BitFlag.decoder(ActivityFlag)
  implicit val encoder: Encoder[List[ActivityFlag]] = BitFlag.encoder
}
