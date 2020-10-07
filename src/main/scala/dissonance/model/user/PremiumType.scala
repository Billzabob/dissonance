package dissonance.model.user

import cats.syntax.all._
import enumeratum._
import io.circe.{Decoder, Encoder}

sealed trait PremiumType extends EnumEntry with Product with Serializable

object PremiumType extends Enum[PremiumType] {
  case object None         extends PremiumType
  case object NitroClassic extends PremiumType
  case object Nitro        extends PremiumType

  val values = findValues

  private val premiumTypeCode: PremiumType => Int = {
    case None         => 0
    case NitroClassic => 1
    case Nitro        => 2
  }

  implicit val premiumTypeDecoder: Decoder[PremiumType] = Decoder[Int].emap { input =>
    Either.fromOption(
      values.find(premiumTypeCode(_) == input),
      s"Unknown premium type ID: $input"
    )
  }

  implicit val premiumTypeEncoder: Encoder[PremiumType] = Encoder[Int].contramap(premiumTypeCode)
}
