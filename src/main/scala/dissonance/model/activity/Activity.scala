package dissonance.model.activity

import dissonance.model.{Emoji, Snowflake}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.{Decoder, Encoder}
import java.time.Instant
import org.http4s.Uri
import org.http4s.circe._

case class Activity(
    name: String,
    `type`: ActivityType,
    url: Option[Uri],
    createdAt: Instant,
    timestamps: Timestamps,
    applicationId: Snowflake,
    details: Option[String],
    state: Option[String],
    emoji: Option[Emoji],
    party: Party,
    assets: Assets,
    secrets: Secrets,
    instance: Boolean,
    flags: List[ActivityFlag]
)

object Activity {
  implicit val config: Configuration              = Configuration.default.withSnakeCaseMemberNames
  implicit val activityDecoder: Decoder[Activity] = deriveConfiguredDecoder
  implicit val activityEncoder: Encoder[Activity] = deriveConfiguredEncoder
}
