package dissonance

import cats.effect._
import cats.implicits._
import java.net.http.HttpClient
import scala.concurrent.duration._

object utils {
  def putStrLn(s: String): IO[Unit] = IO(println(s))

  def fakeResource(i: Int, duration: FiniteDuration)(implicit T: Timer[IO]) =
    Resource.make {
      putStrLn(s"Acquiring Resource $i...") >> IO.sleep(duration) >> putStrLn(s"Acquired Resource $i")
    } { _ => putStrLn(s"Releasing Resource $i...") >> IO.sleep(duration) >> putStrLn(s"Released Resource $i") }

  def javaClient: IO[HttpClient] =
    IO {
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
