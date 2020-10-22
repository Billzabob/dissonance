package dissonance.data.presence

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class ClientStatus(desktop: Option[String], mobile: Option[String], web: Option[String])

object ClientStatus {
  implicit val config: Configuration                      = Configuration.default.withSnakeCaseMemberNames
  implicit val clientStatusDecoder: Decoder[ClientStatus] = deriveConfiguredDecoder
}
