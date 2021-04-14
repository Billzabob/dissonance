package dissonance.data.commands

import cats.syntax.all._
import enumeratum._
import io.circe.{Decoder, Encoder}

sealed trait ApplicationCommandOptionType extends EnumEntry

object ApplicationCommandOptionType extends Enum[ApplicationCommandOptionType] {
  case object SubCommand      extends ApplicationCommandOptionType
  case object SubCommandGroup extends ApplicationCommandOptionType
  case object String          extends ApplicationCommandOptionType
  case object Integer         extends ApplicationCommandOptionType
  case object Boolean         extends ApplicationCommandOptionType
  case object User            extends ApplicationCommandOptionType
  case object Channel         extends ApplicationCommandOptionType
  case object Role            extends ApplicationCommandOptionType

  val values = findValues

  private val applicationCommandOptionTypeCode: ApplicationCommandOptionType => Int = {
    case SubCommand      => 1
    case SubCommandGroup => 2
    case String          => 3
    case Integer         => 4
    case Boolean         => 5
    case User            => 6
    case Channel         => 7
    case Role            => 8
  }

  implicit val applicationCommandOptionTypeDecoder: Decoder[ApplicationCommandOptionType] = Decoder[Int].emap { input =>
    Either.fromOption(
      values.find(applicationCommandOptionTypeCode(_) == input),
      s"Unknown application command option type: $input"
    )
  }

  implicit val applicationCommandOptionTypeEncoder: Encoder[ApplicationCommandOptionType] = Encoder[Int].contramap(applicationCommandOptionTypeCode)
}
