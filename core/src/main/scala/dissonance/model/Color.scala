package dissonance.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.Configuration
import io.circe.syntax._

case class Color(r: Int, g: Int, b: Int)

object Color {
  val red   = new Color(0xff, 0x00, 0x00)
  val green = new Color(0x00, 0xff, 0x00)
  val blue  = new Color(0x00, 0x00, 0xff)

  implicit val config: Configuration        = Configuration.default.withSnakeCaseMemberNames
  implicit val colorEncoder: Encoder[Color] = color => ((color.r << 16) + (color.g << 8) + color.b).asJson
  implicit val colorDecoder: Decoder[Color] = Decoder[Int].map { int =>
    Color(
      int & 0xff0000 >> 16,
      int & 0x00ff00 >> 8,
      int & 0x0000ff
    )
  }
}
