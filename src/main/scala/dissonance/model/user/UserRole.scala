package dissonance.model.user

import dissonance.model.BitFlag
import io.circe.Decoder

sealed trait UserRole extends BitFlag with Product with Serializable

object UserRole {
  case object DiscordEmployee      extends UserRole { val mask = 1 << 0  }
  case object DiscordPartner       extends UserRole { val mask = 1 << 1  }
  case object HypeSquadEvents      extends UserRole { val mask = 1 << 2  }
  case object BugHunterLevel1      extends UserRole { val mask = 1 << 3  }
  case object HouseBravery         extends UserRole { val mask = 1 << 6  }
  case object HouseBrilliance      extends UserRole { val mask = 1 << 7  }
  case object HouseBalance         extends UserRole { val mask = 1 << 8  }
  case object EarlySupporter       extends UserRole { val mask = 1 << 9  }
  case object TeamUser             extends UserRole { val mask = 1 << 10 }
  case object System               extends UserRole { val mask = 1 << 12 }
  case object BugHunterLevel2      extends UserRole { val mask = 1 << 14 }
  case object VerifiedBot          extends UserRole { val mask = 1 << 16 }
  case object VerifiedBotDeveloper extends UserRole { val mask = 1 << 17 }

  val allRoles = List(
    DiscordEmployee,
    DiscordPartner,
    HypeSquadEvents,
    BugHunterLevel1,
    HouseBravery,
    HouseBrilliance,
    HouseBalance,
    EarlySupporter,
    TeamUser,
    System,
    BugHunterLevel2,
    VerifiedBot,
    VerifiedBotDeveloper
  )

  implicit val decoder: Decoder[List[UserRole]] = BitFlag.decoder(allRoles)
}
