package dissonance.model

import io.circe.{Decoder, Encoder}
import java.time.Instant

case class Timestamp(instant: Instant)

object Timestamp {
  implicit val timestampDecoder: Decoder[Timestamp] = Decoder[Long].map(Instant.ofEpochMilli).map(Timestamp.apply)
  implicit val timestampEncoder: Encoder[Timestamp] = Encoder[Long].contramap(_.instant.toEpochMilli)
}
