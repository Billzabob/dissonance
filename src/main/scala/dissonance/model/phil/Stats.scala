package dissonance.model.phil

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

case class Stats(
    win: Boolean,
    kills: Int,
    deaths: Int,
    assists: Int,
    visionScore: Int,
    champLevel: Int,
    totalMinionsKilled: Int,
    totalDamageDealtToChampions: Int,
    neutralMinionsKilled: Int,
    goldEarned: Int,
    doubleKills: Int,
    tripleKills: Int,
    quadraKills: Int,
    pentaKills: Int,
    totalDamageTaken: Int,
    firstBloodKill: Boolean,
    firstTowerKill: Boolean
) {
  lazy val bestAchievement: Option[String] =
    if (pentaKills > 0) Some("a Penta Kill!")
    else if (quadraKills > 0) Some("a Quadra Kill!")
    else if (tripleKills > 0) Some("a Triple Kill!")
    else if (doubleKills > 0) Some("a Double Kill!")
    else if (firstBloodKill) Some("First Blood!")
    else if (firstTowerKill) Some("the first tower kill")
    else None
}

object Stats {
  implicit val statsDecoder: Decoder[Stats] = deriveDecoder
  implicit val statsEncoder: Encoder[Stats] = deriveEncoder
}
