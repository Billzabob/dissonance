package dissonance.data

import enumeratum._
import io.circe.{Decoder, Encoder}

sealed trait UserRole extends EnumEntry with BitFlag with Product with Serializable

object UserRole extends Enum[UserRole] {
  case object DiscordEmployee      extends UserRole { val mask = 1L << 0  }
  case object DiscordPartner       extends UserRole { val mask = 1L << 1  }
  case object HypeSquadEvents      extends UserRole { val mask = 1L << 2  }
  case object BugHunterLevel1      extends UserRole { val mask = 1L << 3  }
  case object HouseBravery         extends UserRole { val mask = 1L << 6  }
  case object HouseBrilliance      extends UserRole { val mask = 1L << 7  }
  case object HouseBalance         extends UserRole { val mask = 1L << 8  }
  case object EarlySupporter       extends UserRole { val mask = 1L << 9  }
  case object TeamUser             extends UserRole { val mask = 1L << 10 }
  case object System               extends UserRole { val mask = 1L << 12 }
  case object BugHunterLevel2      extends UserRole { val mask = 1L << 14 }
  case object VerifiedBot          extends UserRole { val mask = 1L << 16 }
  case object VerifiedBotDeveloper extends UserRole { val mask = 1L << 17 }

  val values = findValues

  implicit val roleDecoder: Decoder[List[UserRole]] = BitFlag.decoder(UserRole)
  implicit val roleEncoder: Encoder[List[UserRole]] = BitFlag.encoder
}
