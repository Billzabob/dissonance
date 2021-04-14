package dissonance.data

import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import io.circe.{Decoder, Encoder}

case class Interaction(
    id: Snowflake,
    applicationId: Snowflake,
    `type`: InteractionType,
    data: Option[ApplicationCommandInteractionData],
    guildId: Option[Snowflake],
    channelId: Option[Snowflake],
    member: Option[Member], // TODO: Technically the GuildMember object is different from a Member object https://discord.com/developers/docs/resources/guild#guild-member-object
    user: Option[User],
    token: String,
    version: Int
)

object Interaction {
  implicit val config: Configuration         = Configuration.default.withSnakeCaseMemberNames
  implicit val decoder: Decoder[Interaction] = deriveConfiguredDecoder
  implicit val encoder: Encoder[Interaction] = deriveConfiguredEncoder
}
