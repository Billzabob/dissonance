package dissonance.model.user

import dissonance.model.BitFlag
import io.circe.Decoder

sealed trait Role extends BitFlag with Product with Serializable

object Role {
  case object DiscordEmployee      extends Role { val mask = 1 << 0  }
  case object DiscordPartner       extends Role { val mask = 1 << 1  }
  case object HypeSquadEvents      extends Role { val mask = 1 << 2  }
  case object BugHunterLevel1      extends Role { val mask = 1 << 3  }
  case object HouseBravery         extends Role { val mask = 1 << 6  }
  case object HouseBrilliance      extends Role { val mask = 1 << 7  }
  case object HouseBalance         extends Role { val mask = 1 << 8  }
  case object EarlySupporter       extends Role { val mask = 1 << 9  }
  case object TeamUser             extends Role { val mask = 1 << 10 }
  case object System               extends Role { val mask = 1 << 12 }
  case object BugHunterLevel2      extends Role { val mask = 1 << 14 }
  case object VerifiedBot          extends Role { val mask = 1 << 16 }
  case object VerifiedBotDeveloper extends Role { val mask = 1 << 17 }

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

  implicit val roleDecoder: Decoder[List[Role]] = BitFlag.decoder(allRoles)
}
