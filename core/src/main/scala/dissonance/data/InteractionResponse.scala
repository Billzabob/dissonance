package dissonance.data

import io.circe.Encoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class InteractionResponse(
    `type`: InteractionResponseType,
    data: Option[InteractionApplicationCommandCallbackData]
)

object InteractionResponse {
  implicit val config: Configuration                                    = Configuration.default.withSnakeCaseMemberNames
  implicit val interactionResponseEncoder: Encoder[InteractionResponse] = deriveConfiguredEncoder
}
