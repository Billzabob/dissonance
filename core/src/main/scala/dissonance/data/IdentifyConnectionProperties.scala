package dissonance.data

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

case class IdentifyConnectionProperties(
    $os: String,
    $browser: String,
    $device: String
)

object IdentifyConnectionProperties {
  implicit val encoder: Encoder[IdentifyConnectionProperties] = deriveEncoder
}
