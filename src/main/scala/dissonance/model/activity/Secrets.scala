package dissonance.model.activity

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class Secrets(join: Option[String], spectate: Option[String], `match`: Option[String])

object Secrets {
  implicit val config: Configuration            = Configuration.default.withSnakeCaseMemberNames
  implicit val secretsDecoder: Decoder[Secrets] = deriveConfiguredDecoder
}
