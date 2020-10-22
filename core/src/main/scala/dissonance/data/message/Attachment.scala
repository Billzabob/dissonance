package dissonance.data.message

import dissonance.data.Snowflake
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import org.http4s.circe._
import org.http4s.Uri

case class Attachment(
    id: Snowflake,
    filename: String,
    size: Int,
    url: Uri,
    proxyUrl: Uri,
    height: Option[Int],
    width: Option[Int]
)

object Attachment {
  implicit val config: Configuration                  = Configuration.default.withSnakeCaseMemberNames
  implicit val attachmentDecoder: Decoder[Attachment] = deriveConfiguredDecoder
}
