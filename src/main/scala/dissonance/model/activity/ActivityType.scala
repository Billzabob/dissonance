package dissonance.model.activity

import io.circe.{Decoder, Encoder}

sealed trait ActivityType extends Product with Serializable

object ActivityType {
  case object Game      extends ActivityType
  case object Streaming extends ActivityType
  case object Listening extends ActivityType
  case object Custom    extends ActivityType

  implicit val activityTypeDecoder: Decoder[ActivityType] = Decoder[Int].emap {
    case 0     => Right(Game)
    case 1     => Right(Streaming)
    case 2     => Right(Listening)
    case 4     => Right(Custom)
    case other => Left(s"Unknown activity type ID: $other")
  }

  implicit val activityTypeEncoder: Encoder[ActivityType] = Encoder[Int].contramap {
    case Game      => 0
    case Streaming => 1
    case Listening => 2
    case Custom    => 4
  }
}
