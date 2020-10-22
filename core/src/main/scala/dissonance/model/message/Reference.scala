package dissonance.model.message

import dissonance.model.Snowflake
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class Reference(
    messageId: Option[Snowflake],
    channelId: Snowflake,
    guildId: Option[Snowflake]
)

object Reference {
  implicit val config: Configuration                = Configuration.default.withSnakeCaseMemberNames
  implicit val referenceDecoder: Decoder[Reference] = deriveConfiguredDecoder
}
