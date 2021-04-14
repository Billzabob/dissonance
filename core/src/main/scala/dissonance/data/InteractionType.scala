package dissonance.data

import cats.syntax.all._
import enumeratum.{Enum, EnumEntry}
import io.circe.{Decoder, Encoder}

sealed trait InteractionType extends EnumEntry with Product with Serializable

object InteractionType extends Enum[InteractionType] {
  case object Ping               extends InteractionType
  case object ApplicationCommand extends InteractionType

  override def values: IndexedSeq[InteractionType] = findValues

  private val interactionTypeCode: InteractionType => Int = {
    case Ping               => 1
    case ApplicationCommand => 2
  }

  implicit val interactionTypeDecoder: Decoder[InteractionType] = Decoder[Int].emap { input =>
    Either.fromOption(
      values.find(interactionTypeCode(_) == input),
      s"Unknown interaction type ID: $input"
    )
  }

  implicit val interactionTypeEncoder: Encoder[InteractionType] = Encoder[Int].contramap(interactionTypeCode)
}
