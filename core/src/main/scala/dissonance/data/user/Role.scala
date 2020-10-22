package dissonance.data.user

import dissonance.data.BitFlag
import enumeratum._
import io.circe.{Decoder, Encoder}

sealed trait Role extends EnumEntry with BitFlag with Product with Serializable

object Role extends Enum[Role] {
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

  val values = findValues

  implicit val roleDecoder: Decoder[List[Role]] = BitFlag.decoder(Role)
  implicit val roleEncoder: Encoder[List[Role]] = BitFlag.encoder
}
