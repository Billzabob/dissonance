package dissonance.model.identify

import dissonance.model.Shard
import dissonance.model.intents.Intent
import dissonance.model.presence.UpdateStatus
import io.circe.Encoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredEncoder

case class Identify(
    token: String,
    properties: IdentifyConnectionProperties,
    compress: Option[Boolean],
    largeThreshold: Option[Int],
    shard: Option[Shard],
    presence: Option[UpdateStatus],
    guildSubscriptions: Option[Boolean],
    intents: List[Intent]
)

object Identify {
  implicit val config: Configuration      = Configuration.default.withSnakeCaseMemberNames
  implicit val encoder: Encoder[Identify] = deriveConfiguredEncoder[Identify].mapJson(_.dropNullValues)
}
