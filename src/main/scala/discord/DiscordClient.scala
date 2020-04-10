package discord

import cats.effect._
import discord.Discord._
import discord.model.{Embed, Message}
import io.circe.Json
import io.circe.syntax._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.client.dsl.io._
import org.http4s.Method._

// TODO: Let user use the underlying client manually if they want
class DiscordClient(val client: Client[IO], token: String) {

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
