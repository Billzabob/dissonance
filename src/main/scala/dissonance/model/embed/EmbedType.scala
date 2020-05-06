package dissonance.model.embed

import enumeratum._
import enumeratum.EnumEntry._

sealed trait EmbedType extends EnumEntry with Uncapitalised with Product with Serializable

object EmbedType extends Enum[EmbedType] with CirceEnum[EmbedType] {
  case object Rich    extends EmbedType
  case object Image   extends EmbedType
  case object Video   extends EmbedType
  case object Gifv    extends EmbedType
  case object Article extends EmbedType
  case object Link    extends EmbedType

  val values = findValues
}
