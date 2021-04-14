package dissonance.data.applicationcommands

case class ApplicationCommandOption(
    `type`: Int,
    name: String,
    description: String,
    required: Boolean,                             // This says default is false
    choices: List[ApplicationCommandOptionChoice], // TODO: optional but make empty list
    options: List[ApplicationCommandOption]        // TODO: optional but make empty list
)
