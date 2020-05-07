package dissonance.model.message

import dissonance.model.embed.Embed
import io.circe.Encoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class MessageEdit(
    content: Option[String],
    embed: Option[Embed],
    flags: Option[List[MessageFlag]]
)

object MessageEdit {
  implicit val config: Configuration                    = Configuration.default.withSnakeCaseMemberNames
  implicit val messageEditEncoder: Encoder[MessageEdit] = deriveConfiguredEncoder
}
