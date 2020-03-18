package discord

import cats.effect.Sync
import fs2.Pipe

object Utils {
  def putStrLn[F[_]: Sync](s: String): F[Unit] = Sync[F].delay(println(s))

  def optionalTake[F[_], A](n: Option[Long]): Pipe[F, A, A] = n match {
    case Some(n) => _.take(n)
    case None    => identity
  }
}
