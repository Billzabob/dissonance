package dissonance.model.guild

import dissonance.model.channel.Channel
import dissonance.model.Event.{PresenceUpdate, Snowflake}
import dissonance.model.{Emoji, Permission}
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import java.time.OffsetDateTime

case class Guild(
    id: Snowflake,
    name: String,
    icon: Option[String],
    splash: Option[String],
    discoverySplash: Option[String],
    owner: Option[Boolean],
    ownerId: Snowflake,
    permissions: List[Permission],
    region: String,
    afkChannelId: Option[Snowflake],
    afkTimeout: Integer,
    embedEnabled: Option[Boolean],
    embedChannelId: Option[Snowflake],
    verificationLevel: Int,
    defaultMessageNotifications: Int,
    explicitContentFilter: Int,
    roles: List[Role],
    emojis: List[Emoji],
    features: List[Feature],
    mfaLevel: Int,
    applicationId: Option[Snowflake],
    widgetEnabled: Option[Boolean],
    widgetChannelId: Option[Snowflake],
    systemChannelId: Option[Snowflake],
    systemChannelFlags: Int,
    rulesChannelId: Option[Snowflake],
    joinedAt: OffsetDateTime,
    large: Boolean,
    unavailable: Boolean,
    memberCount: Int,
    voiceStates: List[VoiceState],
    members: List[Member],
    channels: List[Channel],
    presences: List[PresenceUpdate],
    maxPresences: Option[Int],
    maxMembers: Option[Int],
    vanityUrlCode: Option[String],
    description: Option[String],
    banner: Option[String],
    premiumTier: Int,
    premiumSubscriptionCount: Integer,
    preferredLocale: String,
    publicUpdatesChannelId: Option[Snowflake],
    approximateMemberCount: Option[Int],
    approximatePresenceCount: Option[Int]
)

object Guild {
  implicit val config: Configuration        = Configuration.default.withSnakeCaseMemberNames
  implicit val guildDecoder: Decoder[Guild] = deriveConfiguredDecoder
}
