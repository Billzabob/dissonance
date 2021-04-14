package dissonance.data.commands

import dissonance.data.Snowflake
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import io.circe.{Decoder, Encoder}

case class GuildApplicationCommandPermissions(id: Snowflake, applicationId: Snowflake, guildId: Snowflake, permissions: List[ApplicationCommandPermission])

object GuildApplicationCommandPermissions {
  implicit val config: Configuration                                = Configuration.default.withSnakeCaseMemberNames
  implicit val decoder: Decoder[GuildApplicationCommandPermissions] = deriveConfiguredDecoder
  implicit val encoder: Encoder[GuildApplicationCommandPermissions] = deriveConfiguredEncoder
}
