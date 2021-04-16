package dissonance

import java.net.http.HttpClient

import cats.effect._
import cats.syntax.flatMap._

import scala.concurrent.duration._

object utils {
  def putStrLn[F[_]: Sync](s: String): F[Unit] = Sync[F].delay(println(s))

  def fakeResource[F[_]: Sync: Timer](i: Int, duration: FiniteDuration) =
    Resource.make {
      putStrLn(s"Acquiring Resource $i...") >> Timer[F].sleep(duration) >> putStrLn(s"Acquired Resource $i")
    } { _ => putStrLn(s"Releasing Resource $i...") >> Timer[F].sleep(duration) >> putStrLn(s"Released Resource $i") }

  def javaClient[F[_]: Sync]: F[HttpClient] =
    Sync[F].delay {
      val builder = HttpClient.newBuilder()
      // workaround for https://github.com/http4s/http4s-jdk-http-client/issues/200
      if (Runtime.version().feature() == 11) {
        val params = javax.net.ssl.SSLContext.getDefault().getDefaultSSLParameters()
        params.setProtocols(params.getProtocols().filter(_ != "TLSv1.3"))
        builder.sslParameters(params)
      }
      builder.build()
    }
}
