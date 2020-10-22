package dissonance.data

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import java.time.OffsetDateTime

case class Member(
    user: Option[User],
    nick: Option[String],
    roles: List[Snowflake],
    joinedAt: OffsetDateTime,
    premiumSince: Option[OffsetDateTime],
    deaf: Boolean,
    mute: Boolean
)

object Member {
  implicit val config: Configuration          = Configuration.default.withSnakeCaseMemberNames
  implicit val memberDecoder: Decoder[Member] = deriveConfiguredDecoder
}
