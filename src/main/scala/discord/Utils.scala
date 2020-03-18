package discord

import cats.effect.Sync
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import scala.concurrent.duration._

object utils {
  def putStrLn[F[_]: Sync](s: String): F[Unit] = Sync[F].delay(println(s))

  object json {
    implicit val snakeCaseConfig: Configuration               = Configuration.default.withSnakeCaseMemberNames
    implicit val millisecondsDecoder: Decoder[FiniteDuration] = Decoder[Int].map(_.milliseconds)
  }
}
