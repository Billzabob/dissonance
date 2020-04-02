package discord

import cats.effect._
import cats.implicits._
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import scala.concurrent.duration._

object utils {
  def putStrLn[F[_]: Sync](s: String): F[Unit] = Sync[F].delay(println(s))

  object json {
    implicit val snakeCaseConfig: Configuration               = Configuration.default.withSnakeCaseMemberNames
    implicit val millisecondsDecoder: Decoder[FiniteDuration] = Decoder[Int].map(_.milliseconds)
  }

  def fakeResource[F[_]: Sync: Timer](i: Int, duration: FiniteDuration) =
    Resource.make {
      putStrLn[F](s"Acquiring Resource $i...") >> Timer[F].sleep(duration) >> putStrLn[F](s"Acquired Resource $i")
    } { _ => putStrLn[F](s"Releasing Resource $i...") >> Timer[F].sleep(duration) >> putStrLn[F](s"Released Resource $i") }

}
