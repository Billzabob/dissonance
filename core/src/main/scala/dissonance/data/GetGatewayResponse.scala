package dissonance.data

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class GetGatewayResponse(
    url: String,
    shards: Int,
    sessionStartLimit: SessionStartLimit
)

object GetGatewayResponse {
  implicit val config: Configuration                = Configuration.default.withSnakeCaseMemberNames
  implicit val decoder: Decoder[GetGatewayResponse] = deriveConfiguredDecoder
}
