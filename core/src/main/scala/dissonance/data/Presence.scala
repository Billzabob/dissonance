package dissonance.data

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class Presence(
    user: User,
    guildId: Snowflake,
    status: String,
    activities: List[Activity],
    clientStatus: ClientStatus
)

object Presence {
  implicit val config: Configuration              = Configuration.default.withSnakeCaseMemberNames
  implicit val presenceDecoder: Decoder[Presence] = deriveConfiguredDecoder
}
