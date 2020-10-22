package dissonance.data

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class Party(id: Option[String], size: Option[PartySize])

object Party {
  implicit val config: Configuration        = Configuration.default.withSnakeCaseMemberNames
  implicit val partyDecoder: Decoder[Party] = deriveConfiguredDecoder
  implicit val partyEncoder: Encoder[Party] = deriveConfiguredEncoder
}
