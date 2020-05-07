package dissonance.model.channel

import cats.implicits._
import enumeratum._
import io.circe.{Decoder, Encoder}

sealed trait ChannelType extends EnumEntry with Product with Serializable

object ChannelType extends Enum[ChannelType] {
  case object GuildText          extends ChannelType
  case object DirectMessage      extends ChannelType
  case object GuildVoice         extends ChannelType
  case object GroupDirectMessage extends ChannelType
  case object GuildCategory      extends ChannelType
  case object GuildNews          extends ChannelType
  case object GuildStore         extends ChannelType

  val values = findValues

  private val channelTypeCode: ChannelType => Int = {
    case GuildText          => 0
    case DirectMessage      => 1
    case GuildVoice         => 2
    case GroupDirectMessage => 3
    case GuildCategory      => 4
    case GuildNews          => 5
    case GuildStore         => 6
  }

  implicit val channelTypeDecoder: Decoder[ChannelType] = Decoder[Int].emap { input =>
    Either.fromOption(
      values.find(channelTypeCode(_) == input),
      s"Unknown channel type ID: $input"
    )
  }

  implicit val channelTypeEncoder: Encoder[ChannelType] = Encoder[Int].contramap(channelTypeCode)
}
