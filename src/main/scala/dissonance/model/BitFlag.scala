package dissonance.model

import cats.implicits._
import enumeratum.{Enum, EnumEntry}
import io.circe.Decoder

trait BitFlag extends EnumEntry { val mask: Int }

object BitFlag {

  def decoder[A <: BitFlag](enum: Enum[A]): Decoder[List[A]] =
    Decoder[Option[Int]].map(_.map(flags => enum.values.toList.filter(passesBitMask(flags))).orEmpty)

  private def passesBitMask(flags: Int)(bitFlag: BitFlag) =
    (flags & bitFlag.mask) != 0
}
