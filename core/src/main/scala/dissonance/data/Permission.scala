package dissonance.data

import enumeratum._
import io.circe.{Decoder, Encoder}

sealed trait Permission extends EnumEntry with BitFlag with Product with Serializable

object Permission extends Enum[Permission] {
  case object CreateInstantInvite extends Permission { val mask = 1L << 0  }
  case object KickMembers         extends Permission { val mask = 1L << 1  }
  case object BanMembers          extends Permission { val mask = 1L << 2  }
  case object Administrator       extends Permission { val mask = 1L << 3  }
  case object ManageChannels      extends Permission { val mask = 1L << 4  }
  case object ManageGuild         extends Permission { val mask = 1L << 5  }
  case object AddReactions        extends Permission { val mask = 1L << 6  }
  case object ViewAuditLog        extends Permission { val mask = 1L << 7  }
  case object PrioritySpeaker     extends Permission { val mask = 1L << 8  }
  case object Stream              extends Permission { val mask = 1L << 9  }
  case object ViewChannel         extends Permission { val mask = 1L << 10 }
  case object SendMessages        extends Permission { val mask = 1L << 11 }
  case object SendTtsMessages     extends Permission { val mask = 1L << 12 }
  case object ManageMessages      extends Permission { val mask = 1L << 13 }
  case object EmbedLinks          extends Permission { val mask = 1L << 14 }
  case object AttachFiles         extends Permission { val mask = 1L << 15 }
  case object ReadMessageHistory  extends Permission { val mask = 1L << 16 }
  case object MentionEveryone     extends Permission { val mask = 1L << 17 }
  case object UseExternalEmojis   extends Permission { val mask = 1L << 18 }
  case object ViewGuildInsights   extends Permission { val mask = 1L << 19 }
  case object Connect             extends Permission { val mask = 1L << 20 }
  case object Speak               extends Permission { val mask = 1L << 21 }
  case object MuteMembers         extends Permission { val mask = 1L << 22 }
  case object DeafenMembers       extends Permission { val mask = 1L << 23 }
  case object MoveMembers         extends Permission { val mask = 1L << 24 }
  case object UseVad              extends Permission { val mask = 1L << 25 }
  case object ChangeNickname      extends Permission { val mask = 1L << 26 }
  case object ManageNicknames     extends Permission { val mask = 1L << 27 }
  case object ManageRoles         extends Permission { val mask = 1L << 28 }
  case object ManageWebhooks      extends Permission { val mask = 1L << 29 }
  case object ManageEmojis        extends Permission { val mask = 1L << 30 }

  val values = findValues

  implicit val permissionDecoder: Decoder[List[Permission]] = BitFlag.decoder(Permission)
  implicit val permissionEncoder: Encoder[List[Permission]] = BitFlag.encoder
}
