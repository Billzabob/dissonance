package dissonance.model.activity

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.Configuration

case class PartySize(
    currentSize: Int,
    maxSize: Int
)

object PartySize {
  implicit val config: Configuration       = Configuration.default.withSnakeCaseMemberNames
  implicit val decoder: Decoder[PartySize] = Decoder[(Int, Int)].map(partySize => PartySize(partySize._1, partySize._2))
  implicit val encoder: Encoder[PartySize] = Encoder[(Int, Int)].contramap(partySize => (partySize.currentSize, partySize.maxSize))
}
