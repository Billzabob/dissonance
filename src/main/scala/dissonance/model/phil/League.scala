package dissonance.model.phil

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

case class League(queueType: String, tier: String, rank: String, leaguePoints: Int)

object League {
  implicit val leagueDecoder: Decoder[League] = deriveDecoder
  implicit val leagueEncoder: Encoder[League] = deriveEncoder
}
