package dissonance.data

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import org.http4s.circe._
import org.http4s.Uri

case class Provider(name: Option[String], url: Option[Uri])

object Provider {
  implicit val config: Configuration              = Configuration.default.withSnakeCaseMemberNames
  implicit val providerDecoder: Decoder[Provider] = deriveConfiguredDecoder
  implicit val providerEncoder: Encoder[Provider] = deriveConfiguredEncoder
}
