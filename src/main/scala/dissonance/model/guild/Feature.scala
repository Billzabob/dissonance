package dissonance.model.guild

import io.circe.Decoder
import io.circe.generic.extras.Configuration

sealed trait Feature

object Feature {
  case object InviteSplash         extends Feature
  case object VipRegions           extends Feature
  case object VanityUrl            extends Feature
  case object Verified             extends Feature
  case object Partnered            extends Feature
  case object Public               extends Feature
  case object Commerce             extends Feature
  case object News                 extends Feature
  case object Discoverable         extends Feature
  case object Featurable           extends Feature
  case object AnimatedIcon         extends Feature
  case object Banner               extends Feature
  case object PublicDisabled       extends Feature
  case object WelcomeScreenEnabled extends Feature

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames
  implicit val featureDecoder: Decoder[Feature] = Decoder[String].emap {
    case "INVITE_SPLASH"          => Right(InviteSplash)
    case "VIP_REGIONS"            => Right(VipRegions)
    case "VANITY_URL"             => Right(VanityUrl)
    case "VERIFIED"               => Right(Verified)
    case "PARTNERED"              => Right(Partnered)
    case "PUBLIC"                 => Right(Public)
    case "COMMERCE"               => Right(Commerce)
    case "NEWS"                   => Right(News)
    case "DISCOVERABLE"           => Right(Discoverable)
    case "FEATURABLE"             => Right(Featurable)
    case "ANIMATED_ICON"          => Right(AnimatedIcon)
    case "BANNER"                 => Right(Banner)
    case "PUBLIC_DISABLED"        => Right(PublicDisabled)
    case "WELCOME_SCREEN_ENABLED" => Right(WelcomeScreenEnabled)
    case other                    => Left(s"Unknown feature type: $other")
  }
}
