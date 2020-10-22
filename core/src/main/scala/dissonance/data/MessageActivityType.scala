package dissonance.data

import io.circe.Decoder

sealed trait MessageActivityType extends Product with Serializable

object MessageActivityType {
  case object Join        extends MessageActivityType
  case object Spectate    extends MessageActivityType
  case object Listen      extends MessageActivityType
  case object JoinRequest extends MessageActivityType

  implicit val messageActivityTypeDecoder: Decoder[MessageActivityType] = Decoder[Int].emap {
    case 1     => Right(Join)
    case 2     => Right(Spectate)
    case 3     => Right(Listen)
    case 5     => Right(JoinRequest)
    case other => Left(s"Unknown message activity type ID: $other")
  }
}
