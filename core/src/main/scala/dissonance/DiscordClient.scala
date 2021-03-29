package dissonance

import cats.effect._
import dissonance.data._
import dissonance.Discord._
import dissonance.DiscordClient.WebhookMessage
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredEncoder
import io.circe.syntax._
import io.circe.{Encoder, Json}
import java.io.File
import org.http4s.Method._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.client.dsl.io._
import org.http4s.client.jdkhttpclient.JdkHttpClient
import org.http4s.multipart.{Multipart, Part}
import org.http4s.{Headers, Uri, Request, Status}

class DiscordClient(token: String, client: Client[IO])(implicit cs: ContextShift[IO]) {

  def sendMessage(message: String, channelId: Snowflake, tts: Boolean = false): IO[Message] =
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

  def deleteMessage(channelId: Snowflake, messageId: Snowflake): IO[Unit] =
    client
      .expect[Unit](
        DELETE(
          apiEndpoint.addPath(s"channels/$channelId/messages/$messageId"),
          headers(token)
        )
      )
      .handleErrorWith(_ => IO.unit) // Throws: java.io.IOException: unexpected content length header with 204 response

  def sendEmbed(embed: Embed, channelId: Snowflake): IO[Message] =
    client
      .expect[Message](
        POST(
          // TODO Case class here
          Json.obj("embed" -> embed.asJson),
          apiEndpoint.addPath(s"channels/$channelId/messages"),
          headers(token)
        )
      )

  def sendInteractionResponse(interactionResponse: InteractionResponse, interactionId: Snowflake, interactionToken: String): IO[Unit] =
    client
      .expect[Unit](
        POST(
          interactionResponse,
          apiEndpoint.addPath(s"interactions/$interactionId/$interactionToken/callback")
        )
      )
      .handleErrorWith(_ => IO.unit) // Throws: java.io.IOException: unexpected content length header with 204 response

  def sendEmbedWithFileImage(embed: Embed, file: File, channelId: Snowflake): IO[Message] = {
    val multipart = Multipart[IO](
      Vector(
        Part.fileData[IO]("file", file, blocker),
        Part.formData("payload_json", Json.obj("embed" -> embed.withImage(Image(Some(Uri.unsafeFromString(s"attachment://${file.getName}")), None, None, None)).asJson).noSpaces)
      )
    )
    client.expect[Message](
      POST(
        multipart,
        apiEndpoint.addPath(s"channels/$channelId/messages"),
        (headers(token) :: multipart.headers.toList): _*
      )
    )
  }

  def sendFile(file: File, channelId: Snowflake): IO[Message] = {
    val multipart = Multipart[IO](Vector(Part.fileData[IO]("file", file, blocker)))
    client.expect[Message](
      POST(
        multipart,
        apiEndpoint.addPath(s"channels/$channelId/messages"),
        (headers(token) :: multipart.headers.toList): _*
      )
    )
  }

  def createReaction(channelId: Snowflake, messageId: Snowflake, emoji: String): IO[Unit] =
    client
      .expect[Unit](
        Request[IO](
          method = PUT,
          uri = apiEndpoint.addPath(s"channels/$channelId/messages/$messageId/reactions/$emoji/@me"),
          headers = Headers.of(headers(token))
        )
      )
      .handleErrorWith(_ => IO.unit) // Throws: java.io.IOException: unexpected content length header with 204 response

  def addEmoji(guildId: Snowflake, name: String, emojiData: Array[Byte], roles: List[Snowflake] = Nil): IO[Emoji] = {
    val imageData = "data:;base64," + fs2.Stream.emits(emojiData).through(fs2.text.base64.encode).compile.foldMonoid
    client
      .expect[Emoji](
        POST(
          Json.obj(
            "name"  -> name.asJson,
            "image" -> imageData.asJson,
            "roles" -> roles.asJson
          ),
          apiEndpoint.addPath(s"guilds/$guildId/emojis"),
          headers(token)
        )
      )
  }

  def listEmojis(guildId: Snowflake): IO[List[Emoji]] =
    client
      .expect[List[Emoji]](
        GET(
          apiEndpoint.addPath(s"guilds/$guildId/emojis"),
          headers(token)
        )
      )

  def getChannelMessage(channelId: Snowflake, messageId: Snowflake): IO[Message] =
    client
      .expect[Message](
        GET(
          apiEndpoint.addPath(s"channels/$channelId/messages/$messageId"),
          headers(token)
        )
      )

  def createWebhook(name: String, avatar: Option[ImageDataUri], channelId: Snowflake): IO[Webhook] =
    client
      .expect[Webhook](
        POST(
          // TODO Case class here
          Json.obj(
            "name"   -> name.asJson,
            "avatar" -> avatar.asJson
          ),
          apiEndpoint.addPath(s"channels/$channelId/webhooks"),
          headers(token)
        )
      )

  def getChannelWebhooks(channelId: Snowflake): IO[List[Webhook]] =
    client
      .expect[List[Webhook]](
        GET(
          apiEndpoint.addPath(s"channels/$channelId/webhooks"),
          headers(token)
        )
      )

  def getGuildWebhooks(guildId: Snowflake): IO[List[Webhook]] =
    client
      .expect[List[Webhook]](
        GET(
          apiEndpoint.addPath(s"guilds/$guildId/webhooks"),
          headers(token)
        )
      )

  def getWebhook(webhookId: Snowflake): IO[Webhook] =
    client
      .expect[Webhook](
        GET(
          apiEndpoint.addPath(s"webhooks/$webhookId"),
          headers(token)
        )
      )

  def getWebhookWithToken(webhookId: Snowflake, token: String): IO[Webhook] =
    client
      .expect[Webhook](
        GET(
          apiEndpoint.addPath(s"webhooks/$webhookId/$token")
        )
      )

  def modifyWebhook(webhookId: Snowflake, name: Option[String], avatar: Option[ImageDataUri], channelId: Option[Snowflake]): IO[Webhook] =
    client
      .expect[Webhook](
        PATCH(
          // TODO Case class here
          Json.obj(
            "name"       -> name.asJson,
            "avatar"     -> avatar.asJson,
            "channel_id" -> channelId.asJson
          ),
          apiEndpoint.addPath(s"webhooks/$webhookId"),
          headers(token)
        )
      )

  def modifyWebhookWithToken(webhookId: Snowflake, name: Option[String], avatar: Option[ImageDataUri], channelId: Option[Snowflake], token: String): IO[Webhook] =
    client
      .expect[Webhook](
        PATCH(
          // TODO Case class here
          Json.obj(
            "name"       -> name.asJson,
            "avatar"     -> avatar.asJson,
            "channel_id" -> channelId.asJson
          ),
          apiEndpoint.addPath(s"webhooks/$webhookId/$token")
        )
      )

  def deleteWebhook(webhookId: Snowflake): IO[Status] =
    client
      .status(
        DELETE(
          apiEndpoint.addPath(s"webhooks/$webhookId"),
          headers(token)
        )
      )

  def deleteWebhookWithToken(webhookId: Snowflake, token: String): IO[Status] =
    client
      .status(
        DELETE(
          apiEndpoint.addPath(s"webhooks/$webhookId/$token")
        )
      )

  // TODO: Handle uploading files which requires multipart/form-data
  def executeWebhook(webhook: Webhook, wait: Option[Boolean], webhookMessage: WebhookMessage): IO[Status] =
    client
      .status(
        POST(
          webhookMessage.asJson,
          apiEndpoint
            .addPath(s"webhooks/${webhook.id}/${webhook.token.get}")
            .withQueryParam("wait", wait.getOrElse(false)),
          headers(token)
        )
      )

  // TODO: Add Slack and Github Webhooks
}

object DiscordClient {
  def make(token: String): Resource[IO, DiscordClient] =
    Resource.eval(utils.javaClient.map(javaClient => new DiscordClient(token, JdkHttpClient[IO](javaClient))))

  type AllowedMentions = Unit // TODO: Implement this

  case class WebhookMessage(
      content: String,
      username: Option[String],
      avatarUrl: Option[String],
      tts: Option[Boolean],
      file: Unit,          // TODO: Figure out what this is supposed to be
      embeds: List[Embed], // TODO: Configure this to be max length of 10
      payloadJson: Unit,   // TODO: Figure out what this is supposed to be
      allowedMentions: AllowedMentions
  )

  object WebhookMessage {
    implicit val config: Configuration            = Configuration.default.withSnakeCaseMemberNames
    implicit val encoder: Encoder[WebhookMessage] = deriveConfiguredEncoder
  }
}
