package dissonance.data.commands

import dissonance.data.Snowflake
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import io.circe.{Decoder, Encoder}

case class ApplicationCommand(
    id: Snowflake,
    applicationId: Snowflake,
    name: String,
    description: String,
    options: List[ApplicationCommandOption], // TODO: This is optional but read as empty list
    defaultPermissions: Boolean              // TODO: This is optional but says default true sooooooo
)

object ApplicationCommand {
  implicit val config: Configuration                = Configuration.default.withSnakeCaseMemberNames
  implicit val decoder: Decoder[ApplicationCommand] = deriveConfiguredDecoder
  implicit val encoder: Encoder[ApplicationCommand] = deriveConfiguredEncoder
}
