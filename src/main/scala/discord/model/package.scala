package discord

import io.estatico.newtype.macros.newtype

package object model {
  @newtype case class DiscordId(value: Long)
  @newtype case class AccountName(value: String)
}
