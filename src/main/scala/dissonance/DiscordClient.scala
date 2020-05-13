package dissonance

import cats.effect._
import dissonance.Discord._
import dissonance.DiscordClient.WebhookMessage
import dissonance.model.Snowflake
import dissonance.model.embed.Embed
import dissonance.model.message.Message
import dissonance.model.webhook.Webhook
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredEncoder
import io.circe.{Encoder, Json}
import io.circe.syntax._
import org.http4s.Method._
import org.http4s.Uri
import org.http4s.circe.CirceEntityCodec._
import org.http4s.circe.encodeUri
import org.http4s.client.Client
import org.http4s.client.dsl.io._
import org.http4s.client.jdkhttpclient.JdkHttpClient

class DiscordClient(token: String, client: Client[IO]) {

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

  def createWebhook(name: String, avatar: Option[Uri], channelId: Snowflake): IO[Webhook] =
    client
      .expect[Webhook](
        POST(
          // TODO Case class here
          Json.obj(
            "name" -> name.asJson,
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

  def modifyWebhook(webhookId: Snowflake, name: Option[String], avatar: Option[Uri], channelId: Option[Snowflake]): IO[Webhook] =
    client
      .expect[Webhook](
        PATCH(
          // TODO Case class here
          Json.obj(
            "name" -> name.asJson,
            "avatar" -> avatar.asJson,
            "channel_id" -> channelId.asJson
          ),
          apiEndpoint.addPath(s"webhooks/$webhookId"),
          headers(token)
        )
      )

  def modifyWebhookWithToken(webhookId: Snowflake, name: Option[String], avatar: Option[Uri], channelId: Option[Snowflake], token: String): IO[Webhook] =
    client
      .expect[Webhook](
        PATCH(
          // TODO Case class here
          Json.obj(
            "name" -> name.asJson,
            "avatar" -> avatar.asJson,
            "channel_id" -> channelId.asJson
          ),
          apiEndpoint.addPath(s"webhooks/$webhookId/$token")
        )
      )

  def deleteWebhook(webhookId: Snowflake): IO[Unit] =
    client
    .expect[Unit](
      DELETE(
        apiEndpoint.addPath(s"webhooks/$webhookId"),
        headers(token)
      )
    )

  def deleteWebhookWithToken(webhookId: Snowflake, token: String): IO[Unit] =
    client
      .expect[Unit](
        DELETE(
          apiEndpoint.addPath(s"webhooks/$webhookId/$token")
        )
      )

  // TODO: Handle uploading files which requires multipart/form-data
  def executeWebhook(webhookId: Snowflake, token: String, wait: Option[Boolean], webhookMessage: WebhookMessage): IO[Unit] =
    client
    .expect[Unit](
      POST(
        webhookMessage.asJson,
        apiEndpoint
          .addPath(s"webhooks/$webhookId/$token")
          .withQueryParam("wait", wait.getOrElse(false))
      )
    )

  // TODO: Add Slack and Github Webhooks
}

object DiscordClient {
  def make(token: String)(implicit cs: ContextShift[IO]): Resource[IO, DiscordClient] =
    Resource.liftF(utils.javaClient.map(javaClient => new DiscordClient(token, JdkHttpClient[IO](javaClient))))

  type AllowedMentions = Unit // TODO: Implement this

  case class WebhookMessage(
    content: String,
    username: Option[String],
    avatarUrl: Option[String],
    tts: Option[Boolean],
    file: Unit, // TODO: Figure out what this is supposed to be
    embeds: List[Embed], // TODO: Configure this to be max length of 10
    payloadJson: Unit, // TODO: Figure out what this is supposed to be
    allowedMentions: AllowedMentions
  )

  object WebhookMessage {
    implicit val config: Configuration            = Configuration.default.withSnakeCaseMemberNames
    implicit val encoder: Encoder[WebhookMessage] = deriveConfiguredEncoder
  }
}
