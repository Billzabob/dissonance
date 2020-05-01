package discord

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
    for {
      sslParams <- IO {
                    // Need to user TLSv1.2 because the default of TLSv1.3 is bugged in JDK 11
                    // See here: https://github.com/http4s/http4s-jdk-http-client/issues/200
                    val params = javax.net.ssl.SSLContext.getDefault.getDefaultSSLParameters
                    params.setProtocols(Array("TLSv1.2"))
                    params
                  }
      javaClient <- IO(HttpClient.newBuilder.sslParameters(sslParams).build)
    } yield javaClient

}
