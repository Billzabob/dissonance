package dissonance.model

import enumeratum.EnumEntry.Lowercase
import enumeratum._

sealed trait Status extends EnumEntry with Lowercase with Product with Serializable

object Status extends Enum[Status] with CirceEnum[Status] {
  case object Online    extends Status
  case object DnD       extends Status
  case object Idle      extends Status
  case object Invisible extends Status
  case object Offline   extends Status

  val values = findValues
}
