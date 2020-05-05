package dissonance.model.message

import dissonance.model.BitFlag
import io.circe.Decoder

sealed trait MessageFlag extends BitFlag with Product with Serializable

object MessageFlag {
  case object Crossposted          extends MessageFlag { val mask = 1 << 0 }
  case object IsCrossspost         extends MessageFlag { val mask = 1 << 1 }
  case object SuppressEmbeds       extends MessageFlag { val mask = 1 << 2 }
  case object SourceMessageDeleted extends MessageFlag { val mask = 1 << 3 }
  case object Urgent               extends MessageFlag { val mask = 1 << 4 }

  val allFlags = List(Crossposted, IsCrossspost, SuppressEmbeds, SourceMessageDeleted, Urgent)

  implicit val decoder: Decoder[List[MessageFlag]] = BitFlag.decoder(allFlags)
}
