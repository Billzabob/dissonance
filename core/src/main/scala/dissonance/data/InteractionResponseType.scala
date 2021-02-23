package dissonance.data

import io.circe.Encoder

sealed trait InteractionResponseType extends Product with Serializable

object InteractionResponseType {
  case object Pong                     extends InteractionResponseType
  case object Acknowledge              extends InteractionResponseType
  case object ChannelMessage           extends InteractionResponseType
  case object ChannelMessageWithSource extends InteractionResponseType
  case object AcknowledgeWithSource    extends InteractionResponseType

  implicit val interactionResponseTypeEncoder: Encoder[InteractionResponseType] = Encoder[Int].contramap {
    case Pong                     => 1
    case Acknowledge              => 2
    case ChannelMessage           => 3
    case ChannelMessageWithSource => 4
    case AcknowledgeWithSource    => 5
  }
}
