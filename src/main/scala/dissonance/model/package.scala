package dissonance

import io.estatico.newtype.macros.newtype

package object model {
  // TODO: Implement
  type Snowflake = Long
  // TODO: newtypes for all kinds of Snowflakes
  @newtype case class DiscordId(value: Snowflake) // TODO: Change to Snowflake
  // TODO: newtypes for all plain Strings
  @newtype case class AccountName(value: String)
}
