package dissonance.model.intents

import dissonance.model.BitFlag
import enumeratum.{Enum, EnumEntry}
import io.circe.Encoder

sealed trait Intent extends EnumEntry with BitFlag with Product with Serializable

// https://discord.com/developers/docs/topics/gateway#gateway-intents
object Intent extends Enum[Intent] {
  case object Guilds                 extends Intent { val mask: Int = 1 << 0  }
  case object GuildMembers           extends Intent { val mask: Int = 1 << 1  }
  case object GuildBans              extends Intent { val mask: Int = 1 << 2  }
  case object GuildEmojis            extends Intent { val mask: Int = 1 << 3  }
  case object GuildIntegrations      extends Intent { val mask: Int = 1 << 4  }
  case object GuildWebhooks          extends Intent { val mask: Int = 1 << 5  }
  case object GuildInvites           extends Intent { val mask: Int = 1 << 6  }
  case object GuildVoiceStates       extends Intent { val mask: Int = 1 << 7  }
  case object GuildPresences         extends Intent { val mask: Int = 1 << 8  }
  case object GuildMessages          extends Intent { val mask: Int = 1 << 9  }
  case object GuildMessageReactions  extends Intent { val mask: Int = 1 << 10 }
  case object GuildMessageTyping     extends Intent { val mask: Int = 1 << 11 }
  case object DirectMessages         extends Intent { val mask: Int = 1 << 12 }
  case object DirectMessageReactions extends Intent { val mask: Int = 1 << 13 }
  case object DirectMessageTyping    extends Intent { val mask: Int = 1 << 14 }

  val values: IndexedSeq[Intent] = findValues

  implicit val encoder: Encoder[List[Intent]] = BitFlag.encoder
}
