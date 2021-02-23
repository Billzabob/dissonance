package dissonance.data

import io.circe.{Encoder, Json}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class InteractionApplicationCommandCallbackData(
    tts: Option[Boolean],
    content: String,
    embeds: Option[List[Embed]],
    allowedMentions: Option[Json]
)

object InteractionApplicationCommandCallbackData {
  implicit val config: Configuration                                                                                = Configuration.default.withSnakeCaseMemberNames
  implicit val interactionApplicationCommandCallbackDataEncoder: Encoder[InteractionApplicationCommandCallbackData] = deriveConfiguredEncoder
}
