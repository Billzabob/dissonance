package dissonance.model.activity

import cats.implicits._
import enumeratum._
import io.circe.{Decoder, Encoder}

sealed trait ActivityType extends EnumEntry with Product with Serializable

object ActivityType extends Enum[ActivityType] {
  case object Game      extends ActivityType
  case object Streaming extends ActivityType
  case object Listening extends ActivityType
  case object Custom    extends ActivityType

  val values = findValues

  private val activityTypeCode: ActivityType => Int = {
    case Game      => 0
    case Streaming => 1
    case Listening => 2
    case Custom    => 4
  }

  implicit val activityTypeDecoder: Decoder[ActivityType] = Decoder[Int].emap { input =>
    Either.fromOption(
      values.find(activityTypeCode(_) == input),
      s"Unknown activity type ID: $input"
    )
  }

  implicit val activityTypeEncoder: Encoder[ActivityType] = Encoder[Int].contramap(activityTypeCode)
}
