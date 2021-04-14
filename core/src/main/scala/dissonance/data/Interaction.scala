package dissonance.data

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
