package dissonance.model.phil

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

case class ParticipantIdentity(participantId: Int, player: Player)

object ParticipantIdentity {
  implicit val participantIdentitiesDecoder: Decoder[ParticipantIdentity] = deriveDecoder
  implicit val participantIdentitiesEncoder: Encoder[ParticipantIdentity] = deriveEncoder
}
