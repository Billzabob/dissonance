package dissonance.model.embed

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import org.http4s.circe._
import org.http4s.Uri

case class Author(name: Option[String], url: Option[Uri], iconUrl: Option[Uri], proxyIconUrl: Option[Uri])

object Author {
  implicit val config: Configuration          = Configuration.default.withSnakeCaseMemberNames
  implicit val authorDecoder: Decoder[Author] = deriveConfiguredDecoder
  implicit val authorEncoder: Encoder[Author] = deriveConfiguredEncoder
}
