package dissonance.model.phil

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

case class Player(accountId: String)

object Player {
  implicit val playerDecoder: Decoder[Player] = deriveDecoder
  implicit val playerEncoder: Encoder[Player] = deriveEncoder
}
