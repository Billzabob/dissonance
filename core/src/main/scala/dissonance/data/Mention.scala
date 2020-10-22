package dissonance.data

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class Mention(
    id: Snowflake,
    guildId: Snowflake,
    `type`: ChannelType,
    name: String
)

object Mention {
  implicit val config: Configuration            = Configuration.default.withSnakeCaseMemberNames
  implicit val mentionDecoder: Decoder[Mention] = deriveConfiguredDecoder
}
