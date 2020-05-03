package dissonance.model.guild

import dissonance.model.Color
import dissonance.model.Event.Snowflake
import dissonance.model.Permission
import io.circe.Decoder
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
}
