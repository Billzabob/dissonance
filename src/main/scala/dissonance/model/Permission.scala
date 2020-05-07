package dissonance.model

import enumeratum._
import io.circe.{Decoder, Encoder}

sealed trait Permission extends EnumEntry with BitFlag with Product with Serializable

object Permission extends Enum[Permission] {
  case object CreateInstantInvite extends Permission { val mask = 1 << 0  }
  case object KickMembers         extends Permission { val mask = 1 << 1  }
  case object BanMembers          extends Permission { val mask = 1 << 2  }
  case object Administrator       extends Permission { val mask = 1 << 3  }
  case object ManageChannels      extends Permission { val mask = 1 << 4  }
  case object ManageGuild         extends Permission { val mask = 1 << 5  }
  case object AddReactions        extends Permission { val mask = 1 << 6  }
  case object ViewAuditLog        extends Permission { val mask = 1 << 7  }
  case object PrioritySpeaker     extends Permission { val mask = 1 << 8  }
  case object Stream              extends Permission { val mask = 1 << 9  }
  case object ViewChannel         extends Permission { val mask = 1 << 10 }
  case object SendMessages        extends Permission { val mask = 1 << 11 }
  case object SendTtsMessages     extends Permission { val mask = 1 << 12 }
  case object ManageMessages      extends Permission { val mask = 1 << 13 }
  case object EmbedLinks          extends Permission { val mask = 1 << 14 }
  case object AttachFiles         extends Permission { val mask = 1 << 15 }
  case object ReadMessageHistory  extends Permission { val mask = 1 << 16 }
  case object MentionEveryone     extends Permission { val mask = 1 << 17 }
  case object UseExternalEmojis   extends Permission { val mask = 1 << 18 }
  case object ViewGuildInsights   extends Permission { val mask = 1 << 19 }
  case object Connect             extends Permission { val mask = 1 << 20 }
  case object Speak               extends Permission { val mask = 1 << 21 }
  case object MuteMembers         extends Permission { val mask = 1 << 22 }
  case object DeafenMembers       extends Permission { val mask = 1 << 23 }
  case object MoveMembers         extends Permission { val mask = 1 << 24 }
  case object UseVad              extends Permission { val mask = 1 << 25 }
  case object ChangeNickname      extends Permission { val mask = 1 << 26 }
  case object ManageNicknames     extends Permission { val mask = 1 << 27 }
  case object ManageRoles         extends Permission { val mask = 1 << 28 }
  case object ManageWebhooks      extends Permission { val mask = 1 << 29 }
  case object ManageEmojis        extends Permission { val mask = 1 << 30 }

  val values = findValues

  implicit val permissionDecoder: Decoder[List[Permission]] = BitFlag.decoder(Permission)
  implicit val permissionEncoder: Encoder[List[Permission]] = BitFlag.encoder
}
