package dissonance.data.webhook

import cats.syntax.all._
import enumeratum._
import io.circe.{Decoder, Encoder}

sealed trait WebhookType extends EnumEntry with Product with Serializable

object WebhookType extends Enum[WebhookType] {
  case object Incoming        extends WebhookType
  case object ChannelFollower extends WebhookType

  val values = findValues

  private val webhookTypeCode: WebhookType => Int = {
    case Incoming        => 1
    case ChannelFollower => 2
  }

  implicit val decoder: Decoder[WebhookType] = Decoder[Int].emap { input =>
    Either.fromOption(
      values.find(webhookTypeCode(_) == input),
      s"Unknown webhook type ID: $input"
    )
  }

  implicit val encoder: Encoder[WebhookType] = Encoder[Int].contramap(webhookTypeCode)
}
