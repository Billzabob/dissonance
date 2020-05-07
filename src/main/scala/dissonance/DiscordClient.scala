package dissonance

import cats.effect._
import dissonance.client._
import org.http4s.client.Client
import org.http4s.client.jdkhttpclient.JdkHttpClient

class DiscordClient(token: String, client: Client[IO]) {
  val channel = new ChannelClient(token, client)
}

object DiscordClient {
  def make(token: String)(implicit cs: ContextShift[IO]): Resource[IO, DiscordClient] =
    Resource.liftF(utils.javaClient.map(javaClient => new DiscordClient(token, JdkHttpClient[IO](javaClient))))
}
