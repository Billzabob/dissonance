package dissonance.model.embed

import io.circe.Encoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import org.http4s.circe._
import org.http4s.Uri

case class Image(url: Option[Uri], proxyUrl: Option[Uri], height: Option[Int], width: Option[Int])

object Image {
  implicit val config: Configuration        = Configuration.default.withSnakeCaseMemberNames
  implicit val imageEncoder: Encoder[Image] = deriveConfiguredEncoder
}
