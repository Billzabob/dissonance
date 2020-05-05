package dissonance.model.message

import dissonance.model.Snowflake
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class Application(
    id: Snowflake,
    coverImage: Option[String],
    description: String,
    icon: Option[String],
    name: String
)

object Application {
  implicit val config: Configuration                    = Configuration.default.withSnakeCaseMemberNames
  implicit val applicationDecoder: Decoder[Application] = deriveConfiguredDecoder
}
