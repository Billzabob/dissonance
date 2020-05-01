package dissonance.model.activity

import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class Party(id: Option[String], size: Option[(Int, Int)])

object Party {
  implicit val config: Configuration        = Configuration.default.withSnakeCaseMemberNames
  implicit val partyDecoder: Decoder[Party] = deriveConfiguredDecoder
}
