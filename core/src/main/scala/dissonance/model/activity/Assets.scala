package dissonance.model.activity

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class Assets(
    largeImage: Option[String],
    largeText: Option[String],
    smallImage: Option[String],
    smallText: Option[String]
)

object Assets {
  implicit val config: Configuration          = Configuration.default.withSnakeCaseMemberNames
  implicit val assetsDecoder: Decoder[Assets] = deriveConfiguredDecoder
  implicit val assetsEncoder: Encoder[Assets] = deriveConfiguredEncoder
}
