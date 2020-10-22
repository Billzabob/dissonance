package dissonance.data.channel

import io.circe.Decoder

sealed trait ChannelType extends Product with Serializable

object ChannelType {
  case object GuildText          extends ChannelType
  case object DirectMessage      extends ChannelType
  case object GuildVoice         extends ChannelType
  case object GroupDirectMessage extends ChannelType
  case object GuildCategory      extends ChannelType
  case object GuildNews          extends ChannelType
  case object GuildStore         extends ChannelType

  implicit val channelTypeDecoder: Decoder[ChannelType] = Decoder[Int].emap {
    case 0     => Right(GuildText)
    case 1     => Right(DirectMessage)
    case 2     => Right(GuildVoice)
    case 3     => Right(GroupDirectMessage)
    case 4     => Right(GuildCategory)
    case 5     => Right(GuildNews)
    case 6     => Right(GuildStore)
    case other => Left(s"Unknown channel type ID: $other")
  }
}
