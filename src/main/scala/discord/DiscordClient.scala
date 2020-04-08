package discord

import cats.effect._
import cats.implicits._
import discord.model.Message
import io.circe.Json
import io.circe.syntax._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.client.dsl.io._
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.Method._

class DiscordClient(client: Client[IO], token: String) {

  // TODO: Duplicated from Discord.scala
  val apiUri = uri"https://discordapp.com/api"

  // TODO: Duplicated from Discord.scala
  def headers(token: String) =
    Authorization(Credentials.Token("Bot".ci, token))

  def sendMessage(message: Message): IO[Unit] =
    client
      .expect[Json](
        POST(
          Json.obj("content" -> "pong".asJson),
          apiUri.addPath(s"channels/${message.channelId}/messages"),
          headers(token)
        )
      )
      .void
}
