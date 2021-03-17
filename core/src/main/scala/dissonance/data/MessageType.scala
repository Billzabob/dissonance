package dissonance.data

import io.circe.Decoder

sealed trait MessageType extends Product with Serializable

object MessageType {
  case object Default                           extends MessageType
  case object RecipientAdd                      extends MessageType
  case object RecipientRemove                   extends MessageType
  case object Call                              extends MessageType
  case object ChannelNameChange                 extends MessageType
  case object ChannelIconChange                 extends MessageType
  case object ChannelPinnedMessage              extends MessageType
  case object GuildMemberJoin                   extends MessageType
  case object UserPremiumGuildSubscription      extends MessageType
  case object UserPremiumGuildSubscriptionTier1 extends MessageType
  case object UserPremiumGuildSubscriptionTier2 extends MessageType
  case object UserPremiumGuildSubscriptionTier3 extends MessageType
  case object ChannelFollowAdd                  extends MessageType
  case object GuildDiscoveryDisqualified        extends MessageType
  case object GuildDiscoveryRequalified         extends MessageType
  case object Reply                             extends MessageType
  case object ApplicationCommand                extends MessageType

  implicit val messageTypeDecoder: Decoder[MessageType] = Decoder[Int].emap {
    case 0     => Right(Default)
    case 1     => Right(RecipientAdd)
    case 2     => Right(RecipientRemove)
    case 3     => Right(Call)
    case 4     => Right(ChannelNameChange)
    case 5     => Right(ChannelIconChange)
    case 6     => Right(ChannelPinnedMessage)
    case 7     => Right(GuildMemberJoin)
    case 8     => Right(UserPremiumGuildSubscription)
    case 9     => Right(UserPremiumGuildSubscriptionTier1)
    case 10    => Right(UserPremiumGuildSubscriptionTier2)
    case 11    => Right(UserPremiumGuildSubscriptionTier3)
    case 12    => Right(ChannelFollowAdd)
    case 14    => Right(GuildDiscoveryDisqualified)
    case 15    => Right(GuildDiscoveryRequalified)
    case 19    => Right(Reply)
    case 20    => Right(ApplicationCommand)
    case other => Left(s"Unknown message type ID: $other")
  }
}
