package dissonance.model

import cats.implicits._
import io.circe.Decoder

trait BitFlag { val mask: Int }

object BitFlag {

  def decoder[A <: BitFlag](allFlags: List[A]): Decoder[List[A]] =
    Decoder[Option[Int]].map(_.map(flags => allFlags.filter(passesBitMask(flags))).orEmpty)

  private def passesBitMask(flags: Int)(bitFlag: BitFlag) =
    (flags & bitFlag.mask) != 0
}
