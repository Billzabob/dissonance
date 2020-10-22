package dissonance

import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype

package object data {
  // TODO: Implement
  type Snowflake = Long
  // TODO: newtypes for all kinds of Snowflakes
  @newtype case class DiscordId(value: Snowflake)

  object DiscordId {
    implicit val idDecoder: Decoder[DiscordId] = Decoder[Long].map(DiscordId.apply)
    implicit val idEncoder: Encoder[DiscordId] = Encoder[Long].contramap(_.value)
  }
  // TODO: newtypes for all plain Strings
  @newtype case class AccountName(value: String)
}
