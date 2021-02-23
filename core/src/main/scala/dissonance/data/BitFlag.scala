package dissonance.data

import cats.syntax.all._
import enumeratum.{Enum, EnumEntry}
import io.circe.{Decoder, Encoder}

trait BitFlag extends EnumEntry { val mask: Long }

object BitFlag {

  def decoder[A <: BitFlag](enum: Enum[A]): Decoder[List[A]] =
    Decoder[Option[Long]].map(_.map(flags => enum.values.toList.filter(passesBitMask(flags))).orEmpty)

  def encoder[A <: BitFlag]: Encoder[List[A]] =
    Encoder[Long].contramap(_.map(_.mask).combineAll)

  private def passesBitMask(flags: Long)(bitFlag: BitFlag) =
    (flags & bitFlag.mask) != 0
}
