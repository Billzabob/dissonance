package dissonance.model.phil

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

case class Participant(participantId: Int, championId: Int, teamId: Int, stats: Stats)

object Participant {
  implicit val participantDecoder: Decoder[Participant] = deriveDecoder
  implicit val participantEncoder: Encoder[Participant] = deriveEncoder
}
