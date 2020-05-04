package dissonance.model.user

import io.circe.Decoder

sealed trait PremiumType extends Product with Serializable

object PremiumType {
  case object None         extends PremiumType
  case object NitroClassic extends PremiumType
  case object Nitro        extends PremiumType

  implicit val premiumTypeDecoder: Decoder[PremiumType] = Decoder[Int].emap {
    case 0     => Right(None)
    case 1     => Right(NitroClassic)
    case 2     => Right(Nitro)
    case other => Left(s"Unknown premium type ID: $other")
  }
}
