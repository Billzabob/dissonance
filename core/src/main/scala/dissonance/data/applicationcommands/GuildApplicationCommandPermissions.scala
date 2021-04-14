package dissonance.data.applicationcommands

import dissonance.data.Snowflake

case class GuildApplicationCommandPermissions(id: Snowflake, applicationId: Snowflake, guildId: Snowflake, permissions: List[ApplicationCommandPermissions])
