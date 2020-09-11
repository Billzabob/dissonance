package dissonance.model.phil

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

case class Stats(
    win: Boolean,
    kills: Int,
    deaths: Int,
    assists: Int,
    visionScore: Int,
    totalMinionsKilled: Int,
    neutralMinionsKilled: Int,
    goldEarned: Int
)

object Stats {
  implicit val statsDecoder: Decoder[Stats] = deriveDecoder
  implicit val statsEncoder: Encoder[Stats] = deriveEncoder
}
