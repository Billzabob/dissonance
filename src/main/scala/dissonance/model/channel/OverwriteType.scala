package dissonance.model.channel

import io.circe.Decoder

sealed trait OverwriteType extends Product with Serializable

object OverwriteType {
  case object Role   extends OverwriteType
  case object Member extends OverwriteType

  implicit val channelTypeDecoder: Decoder[OverwriteType] = Decoder[String].emap {
    case "role"   => Right(Role)
    case "member" => Right(Member)
    case other    => Left(s"Unknown overwrite type: $other")
  }
}
