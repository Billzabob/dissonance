package dissonance.model.embed

import io.circe.Encoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import org.http4s.circe._
import org.http4s.Uri

case class Footer(text: String, iconUrl: Option[Uri], proxyIconUrl: Uri)

object Footer {
  implicit val config: Configuration          = Configuration.default.withSnakeCaseMemberNames
  implicit val footerEncoder: Encoder[Footer] = deriveConfiguredEncoder
}
