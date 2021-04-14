package dissonance.data.applicationcommands

import dissonance.data.Snowflake

case class ApplicationCommand(
    id: Snowflake,
    applicationId: Snowflake,
    name: String,
    description: String,
    options: List[ApplicationCommandOption], // TODO: This is optional but read as empty list
    defaultPermissions: Boolean              // TODO: This is optional but says default true sooooooo
)
