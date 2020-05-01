package dissonance

import io.estatico.newtype.macros.newtype

package object model {
  @newtype case class DiscordId(value: Long) // TODO: Change to Snowflake
  @newtype case class AccountName(value: String)
}
