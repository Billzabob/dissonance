package dissonance.model.user

import io.circe.Decoder
import cats.implicits._

sealed trait UserRole extends Product with Serializable {
  val shiftAmount: Int
}

object UserRole {
  case object DiscordEmployee      extends UserRole { val shiftAmount = 0  }
  case object DiscordPartner       extends UserRole { val shiftAmount = 1  }
  case object HypeSquadEvents      extends UserRole { val shiftAmount = 2  }
  case object BugHunterLevel1      extends UserRole { val shiftAmount = 3  }
  case object HouseBravery         extends UserRole { val shiftAmount = 6  }
  case object HouseBrilliance      extends UserRole { val shiftAmount = 7  }
  case object HouseBalance         extends UserRole { val shiftAmount = 8  }
  case object EarlySupporter       extends UserRole { val shiftAmount = 9  }
  case object TeamUser             extends UserRole { val shiftAmount = 10 }
  case object System               extends UserRole { val shiftAmount = 12 }
  case object BugHunterLevel2      extends UserRole { val shiftAmount = 14 }
  case object VerifiedBot          extends UserRole { val shiftAmount = 16 }
  case object VerifiedBotDeveloper extends UserRole { val shiftAmount = 17 }

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

  implicit val decoder = Decoder[Option[Int]].map(_.map(flags => allRoles.filter(passesBitMask(flags))).orEmpty)

  private def passesBitMask(flags: Int)(role: UserRole) = {
    val roleBitMask = 1 << role.shiftAmount
    (flags & roleBitMask) != 0
  }
}
