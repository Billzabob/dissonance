package dissonance.model

import cats.syntax.all._
import enumeratum.{Enum, EnumEntry}
import io.circe.{Decoder, Encoder}

trait BitFlag extends EnumEntry { val mask: Int }

object BitFlag {

  def decoder[A <: BitFlag](enum: Enum[A]): Decoder[List[A]] =
    Decoder[Option[Int]].map(_.map(flags => enum.values.toList.filter(passesBitMask(flags))).orEmpty)

  def encoder[A <: BitFlag]: Encoder[List[A]] =
    Encoder[Int].contramap(_.map(_.mask).combineAll)

  private def passesBitMask(flags: Int)(bitFlag: BitFlag) =
    (flags & bitFlag.mask) != 0
}
