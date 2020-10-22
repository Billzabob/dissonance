package dissonance.data

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class Ban(guildId: Snowflake, user: User)

object Ban {
  implicit val config: Configuration         = Configuration.default.withSnakeCaseMemberNames
  implicit val guildBanDecoder: Decoder[Ban] = deriveConfiguredDecoder
}
