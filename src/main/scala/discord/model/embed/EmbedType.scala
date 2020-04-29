package discord.model.embed

import io.circe.Encoder
import io.circe.generic.extras.Configuration
import io.circe.syntax._

sealed trait EmbedType extends Product with Serializable

object EmbedType {
  case object Rich    extends EmbedType
  case object Image   extends EmbedType
  case object Video   extends EmbedType
  case object Gifv    extends EmbedType
  case object Article extends EmbedType
  case object Link    extends EmbedType

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames
  implicit val embedTypeEncoder: Encoder[EmbedType] = _ match {
    case Rich    => "rich".asJson
    case Image   => "image".asJson
    case Video   => "video".asJson
    case Gifv    => "gifv".asJson
    case Article => "article".asJson
    case Link    => "link".asJson
  }
}
