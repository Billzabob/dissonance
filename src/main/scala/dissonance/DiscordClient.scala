package dissonance

import cats.effect._
import dissonance.Discord._
import dissonance.model.embed.Embed
import dissonance.model.Message
import io.circe.Json
import io.circe.syntax._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.client.dsl.io._
import org.http4s.client.jdkhttpclient.JdkHttpClient
import org.http4s.Method._

class DiscordClient(token: String, client: Client[IO]) {

  def sendMessage(message: String, channelId: String, tts: Boolean = false): IO[Message] =
    client
      .expect[Message](
        POST(
          // TODO Case class here
          Json.obj(
            "content" -> message.asJson,
            "tts"     -> tts.asJson
          ),
          apiEndpoint.addPath(s"channels/$channelId/messages"),
          headers(token)
        )
      )

  def sendEmbed(embed: Embed, channelId: String): IO[Message] =
    client
      .expect[Message](
        POST(
          // TODO Case class here
          Json.obj("embed" -> embed.asJson),
          apiEndpoint.addPath(s"channels/$channelId/messages"),
          headers(token)
        )
      )
}

object DiscordClient {
  def make(token: String)(implicit cs: ContextShift[IO]): Resource[IO, DiscordClient] =
    Resource.liftF(utils.javaClient.map(javaClient => new DiscordClient(token, JdkHttpClient[IO](javaClient))))
}
