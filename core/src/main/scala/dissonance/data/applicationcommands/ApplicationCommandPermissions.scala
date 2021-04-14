package dissonance.data.applicationcommands

import dissonance.data.Snowflake

case class ApplicationCommandPermissions(id: Snowflake, `type`: ApplicationCommandPermissionType, permission: Boolean)
