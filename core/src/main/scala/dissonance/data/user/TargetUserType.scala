package dissonance.data.user

import io.circe.Decoder

sealed trait TargetUserType extends Product with Serializable

object TargetUserType {
  case object Stream extends TargetUserType

  implicit val targetUserTypeDecoder: Decoder[TargetUserType] = Decoder[Int].emap {
    case 1     => Right(Stream)
    case other => Left(s"Unknown target user type ID: $other")
  }
}
