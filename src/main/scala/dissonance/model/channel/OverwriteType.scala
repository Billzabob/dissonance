package dissonance.model.channel

import enumeratum._
import enumeratum.EnumEntry._

sealed trait OverwriteType extends EnumEntry with Uncapitalised with Product with Serializable

object OverwriteType extends Enum[OverwriteType] with CirceEnum[OverwriteType] {
  case object Role   extends OverwriteType
  case object Member extends OverwriteType

  val values = findValues
}
