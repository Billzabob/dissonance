package dissonance.data

import enumeratum._
import io.circe.Decoder

sealed trait MessageFlag extends EnumEntry with BitFlag with Product with Serializable

object MessageFlag extends Enum[MessageFlag] {
  case object Crossposted          extends MessageFlag { val mask = 1L << 0 }
  case object IsCrossspost         extends MessageFlag { val mask = 1L << 1 }
  case object SuppressEmbeds       extends MessageFlag { val mask = 1L << 2 }
  case object SourceMessageDeleted extends MessageFlag { val mask = 1L << 3 }
  case object Urgent               extends MessageFlag { val mask = 1L << 4 }

  val values = findValues

  implicit val decoder: Decoder[List[MessageFlag]] = BitFlag.decoder(MessageFlag)
}
