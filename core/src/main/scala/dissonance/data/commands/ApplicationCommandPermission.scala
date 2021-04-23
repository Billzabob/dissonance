package dissonance.data.commands

import dissonance.data.Snowflake
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

case class ApplicationCommandPermission(id: Snowflake, `type`: ApplicationCommandPermissionType, permission: Boolean)

object ApplicationCommandPermission {
  implicit val applicationCommandPermissionDecoder: Decoder[ApplicationCommandPermission] = deriveDecoder
  implicit val applicationCommandPermissionEncoder: Encoder[ApplicationCommandPermission] = deriveEncoder
}
