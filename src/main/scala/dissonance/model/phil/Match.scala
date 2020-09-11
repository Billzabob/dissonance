package dissonance.model.phil

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

case class Match(gameId: Long, participants: List[Participant], participantIdentities: List[ParticipantIdentity], gameDuration: Int) {
  def player(accountId: String): Participant = {
    val result = for {
      pId <- participantIdentities.find(_.player.accountId == accountId)
      ps  <- participants.find(_.participantId == pId.participantId)
    } yield ps
    result.get
  }
}

object Match {
  implicit val matchDecoder: Decoder[Match] = deriveDecoder
  implicit val matchEncoder: Encoder[Match] = deriveEncoder
}
