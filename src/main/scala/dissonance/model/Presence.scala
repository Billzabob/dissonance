package dissonance.model

import dissonance.model.activity.Activity
import dissonance.model.Event.Snowflake
import dissonance.model.Presence._
import dissonance.model.user.User
import io.circe.{Json, Decoder}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import java.time.OffsetDateTime

case class Presence(
    user: User,
    roles: List[Snowflake],
    game: Option[Activity],
    guildId: Snowflake,
    status: String,
    activities: List[Activity],
    clientStatus: ClientStatus,
    premiumSince: Option[OffsetDateTime],
    nick: Option[String]
)

object Presence {
  // TODO: Implement
  type ClientStatus = Json

  implicit val config: Configuration              = Configuration.default.withSnakeCaseMemberNames
  implicit val presenceDecoder: Decoder[Presence] = deriveConfiguredDecoder
}
