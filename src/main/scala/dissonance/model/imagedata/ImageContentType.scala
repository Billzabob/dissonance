package dissonance.model.imagedata

import enumeratum._

sealed trait ImageContentType extends EnumEntry with Product with Serializable

object ImageContentType extends Enum[ImageContentType] {
  case object jpeg extends ImageContentType
  case object png  extends ImageContentType
  case object gif  extends ImageContentType

  val values = findValues
}
