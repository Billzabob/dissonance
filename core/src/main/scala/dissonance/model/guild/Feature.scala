package dissonance.model.guild

import enumeratum._
import enumeratum.EnumEntry._

sealed trait Feature extends EnumEntry with UpperSnakecase with Product with Serializable

object Feature extends Enum[Feature] with CirceEnum[Feature] {
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

  val values = findValues
}
