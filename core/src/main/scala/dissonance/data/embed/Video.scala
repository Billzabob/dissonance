package dissonance.data.embed

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import org.http4s.circe._
import org.http4s.Uri

case class Video(url: Option[Uri], height: Option[Int], width: Option[Int])

object Video {
  implicit val config: Configuration        = Configuration.default.withSnakeCaseMemberNames
  implicit val videoDecoder: Decoder[Video] = deriveConfiguredDecoder
  implicit val videoEncoder: Encoder[Video] = deriveConfiguredEncoder
}
