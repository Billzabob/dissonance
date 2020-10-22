package dissonance.data.guild

import dissonance.data.Color
import dissonance.data.{Permission, Snowflake}
import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class Role(
    id: Snowflake,
    name: String,
    color: Color,
    hoist: Boolean,
    position: Int,
    permissions: List[Permission],
    managed: Boolean,
    mentionable: Boolean
)

object Role {
  implicit val config: Configuration      = Configuration.default.withSnakeCaseMemberNames
  implicit val roleDecoder: Decoder[Role] = deriveConfiguredDecoder
  implicit val roleEncoder: Encoder[Role] = deriveConfiguredEncoder
}
