package discord.model.user

import io.circe.Decoder

sealed trait PremiumType

object PremiumType {
  case object None         extends PremiumType
  case object NitroClassic extends PremiumType
  case object Nitro        extends PremiumType

  implicit val messageDecoder: Decoder[PremiumType] = Decoder[Int].map {
    case 0 => None
    case 1 => NitroClassic
    case 2 => Nitro
  }
}
