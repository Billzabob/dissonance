package dissonance.model.embed

import io.circe.Encoder
import io.circe.generic.extras.Configuration
import io.circe.syntax._

case class Color(r: Int, g: Int, b: Int)

object Color {
  val red   = new Color(0xFF, 0x00, 0x00)
  val green = new Color(0x00, 0xFF, 0x00)
  val blue  = new Color(0x00, 0x00, 0xFF)

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames
  implicit val colorEncoder: Encoder[Color] =
    color => ((color.r << 16) + (color.g << 8) + color.b).asJson
}
