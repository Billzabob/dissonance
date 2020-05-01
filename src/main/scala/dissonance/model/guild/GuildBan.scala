package dissonance.model.guild

import dissonance.model.DispatchEvent.Snowflake
import dissonance.model.user.User
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class GuildBan(guildId: Snowflake, user: User)

object GuildBan {
  implicit val config: Configuration              = Configuration.default.withSnakeCaseMemberNames
  implicit val guildBanDecoder: Decoder[GuildBan] = deriveConfiguredDecoder
}
