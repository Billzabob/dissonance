package dissonance.data

import enumeratum._
import io.circe.{Decoder, Encoder}

sealed trait InteractionCallbackFlag extends EnumEntry with BitFlag with Product with Serializable

object InteractionCallbackFlag extends Enum[InteractionCallbackFlag] {
  case object Ephemeral extends InteractionCallbackFlag { val mask = 1L << 6 }

  val values = findValues

  implicit val interactionCallbackFlagDecoder: Decoder[List[InteractionCallbackFlag]] = BitFlag.decoder(InteractionCallbackFlag)
  implicit val interactionCallbackFlagEncoder: Encoder[List[InteractionCallbackFlag]] = BitFlag.encoder
}
