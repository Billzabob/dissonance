package dissonance.data

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class GuildRole(
    id: Snowflake,
    name: String,
    color: Color,
    hoist: Boolean,
    position: Int,
    permissions: List[Permission],
    managed: Boolean,
    mentionable: Boolean
)

object GuildRole {
  implicit val config: Configuration           = Configuration.default.withSnakeCaseMemberNames
  implicit val roleDecoder: Decoder[GuildRole] = deriveConfiguredDecoder
  implicit val roleEncoder: Encoder[GuildRole] = deriveConfiguredEncoder
}
