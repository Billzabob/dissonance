package dissonance.data.util

import cats.syntax.all._
import io.circe._

object CirceUtils {
  implicit val decodeEither: Decoder[Either[Int, String]] = (c: HCursor) =>
    c.value.asNumber
      .flatMap(_.toInt)
      .map(_.asLeft[String])
      .orElse(c.value.asString.map(_.asRight[Int]))
      .fold(DecodingFailure("Value is  not an Either[String, Int", c.history).asLeft[Either[Int, String]])(_.asRight[DecodingFailure])

  implicit val encodeEither: Encoder[Either[Int, String]] = (v: Either[Int, String]) => v.fold(Json.fromInt, Json.fromString)
}
