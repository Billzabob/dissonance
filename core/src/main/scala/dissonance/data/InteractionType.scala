package dissonance.data

import io.circe.Decoder

sealed trait InteractionType extends Product with Serializable

object InteractionType {
  case object Ping               extends InteractionType
  case object ApplicationCommand extends InteractionType

  implicit val targetUserTypeDecoder: Decoder[InteractionType] = Decoder[Int].emap {
    case 1     => Right(Ping)
    case 2     => Right(ApplicationCommand)
    case other => Left(s"Unknown interaction type ID: $other")
  }
}
