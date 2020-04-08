package discord

import cats.effect._
import cats.implicits._
import discord.Discord._
import discord.model.Message
import io.circe.Json
import io.circe.syntax._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.client.dsl.io._
import org.http4s.Method._

class DiscordClient(client: Client[IO], token: String) {

  def sendMessage(message: Message): IO[Unit] =
    client
      .expect[Json](
        POST(
          Json.obj("content" -> message.content.asJson),
          apiEndpoint.addPath(s"channels/${message.channelId}/messages"),
          headers(token)
        )
      )
      .void
}
