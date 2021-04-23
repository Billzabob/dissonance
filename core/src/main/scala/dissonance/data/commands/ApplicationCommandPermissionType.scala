package dissonance.data.commands

import cats.syntax.all._
import enumeratum.{EnumEntry, _}
import io.circe.{Decoder, Encoder}

sealed trait ApplicationCommandPermissionType extends EnumEntry

object ApplicationCommandPermissionType extends Enum[ApplicationCommandPermissionType] {
  case object Role extends ApplicationCommandPermissionType
  case object User extends ApplicationCommandPermissionType

  val values = findValues

  private val applicationCommandPermissionTypeCode: ApplicationCommandPermissionType => Int = {
    case Role => 1
    case User => 2
  }

  implicit val applicationCommandPermissionTypeDecoder: Decoder[ApplicationCommandPermissionType] = Decoder[Int].emap { input =>
    Either.fromOption(
      values.find(applicationCommandPermissionTypeCode(_) == input),
      s"Unknown application command permission type: $input"
    )
  }

  implicit val applicationCommandPermissionTypeEncoder: Encoder[ApplicationCommandPermissionType] = Encoder[Int].contramap(applicationCommandPermissionTypeCode)
}
