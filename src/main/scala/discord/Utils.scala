package discord

import cats.effect._
import cats.implicits._
import scala.concurrent.duration._

object utils {
  def putStrLn(s: String): IO[Unit] = IO(println(s))

  def fakeResource(i: Int, duration: FiniteDuration)(implicit T: Timer[IO]) =
    Resource.make {
      putStrLn(s"Acquiring Resource $i...") >> IO.sleep(duration) >> putStrLn(s"Acquired Resource $i")
    } { _ => putStrLn(s"Releasing Resource $i...") >> IO.sleep(duration) >> putStrLn(s"Released Resource $i") }

}
