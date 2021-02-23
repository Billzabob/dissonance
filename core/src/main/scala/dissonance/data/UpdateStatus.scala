package dissonance.data

import io.circe.Encoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredEncoder

case class UpdateStatus(
    since: Option[Int],
    activities: Option[List[Activity]],
    status: Status,
    afk: Boolean
)

object UpdateStatus {
  implicit val config: Configuration          = Configuration.default.withSnakeCaseMemberNames
  implicit val encoder: Encoder[UpdateStatus] = deriveConfiguredEncoder
}
