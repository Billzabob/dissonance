package dissonance

import java.io.File

import cats.Applicative
import java.io.File

import cats.effect._
import cats.syntax.all._
import dissonance.Discord._
import dissonance.DiscordClient.WebhookMessage
import dissonance.data._
import dissonance.data.commands.{ApplicationCommand, ApplicationCommandOption, ApplicationCommandPermission, GuildApplicationCommandPermissions}
import dissonance.data._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredEncoder
import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.http4s.Method._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.client.Client
import org.http4s.client.jdkhttpclient.JdkHttpClient
import org.http4s.multipart.{Multipart, Part}
import org.http4s.{Headers, Request, Status, Uri}
import org.http4s.{Request, Status, Uri}

class DiscordClient[F[_]: Sync](token: String, client: Client[F])(implicit cs: ContextShift[F]) {

  def sendMessage(message: String, channelId: Snowflake, tts: Boolean = false): F[Message] =
    client
      .expect[Message](
        Request[F]()
          .withMethod(POST)
          .withUri(apiEndpoint.addPath(s"channels/$channelId/messages"))
          .withEntity(
            // TODO Case class here
            Json.obj(
              "content" -> message.asJson,
              "tts"     -> tts.asJson
            )
          )
          .withHeaders(headers(token))
      )

  def deleteMessage(channelId: Snowflake, messageId: Snowflake): F[Unit] =
    client
      .expect[Unit](
        Request[F]()
          .withMethod(DELETE)
          .withUri(apiEndpoint.addPath(s"channels/$channelId/messages/$messageId"))
          .withHeaders(headers(token))
      )
      .handleErrorWith(_ => Applicative[F].unit) // Throws: java.io.IOException: unexpected content length header with 204 response

  def sendEmbed(embed: Embed, channelId: Snowflake): F[Message] =
    client
      .expect[Message](
        Request[F]()
          .withMethod(POST)
          .withUri(apiEndpoint.addPath(s"channels/$channelId/messages"))
          .withEntity(
            // TODO Case class here
            Json.obj("embed" -> embed.asJson)
          )
          .withHeaders(headers(token))
      )

  def sendInteractionResponse(interactionResponse: InteractionResponse, interactionId: Snowflake, interactionToken: String): F[Unit] =
    client
      .expect[Unit](
        Request[F]()
          .withMethod(POST)
          .withUri(apiEndpoint.addPath(s"interactions/$interactionId/$interactionToken/callback"))
          .withEntity(interactionResponse)
      )
      .handleErrorWith(_ => Applicative[F].unit) // Throws: java.io.IOException: unexpected content length header with 204 response

  def sendEmbedWithFileImage(embed: Embed, file: File, channelId: Snowflake, blocker: Blocker): F[Message] = {
    val multipart = Multipart[F](
      Vector(
        Part.fileData[F]("file", file, blocker),
        Part.formData("payload_json", Json.obj("embed" -> embed.withImage(Image(Some(Uri.unsafeFromString(s"attachment://${file.getName}")), None, None, None)).asJson).noSpaces)
      )
    )
    client
      .expect[Message](
        Request[F]()
          .withMethod(POST)
          .withUri(apiEndpoint.addPath(s"channels/$channelId/messages"))
          .withEntity(multipart)
          .withHeaders(headers(token) :: multipart.headers.toList: _*)
      )
  }

  def sendFile(file: File, channelId: Snowflake, blocker: Blocker): F[Message] = {
    val multipart = Multipart[F](Vector(Part.fileData[F]("file", file, blocker)))
    client
      .expect[Message](
        Request[F]()
          .withMethod(POST)
          .withUri(apiEndpoint.addPath(s"channels/$channelId/messages"))
          .withEntity(multipart)
          .withHeaders(headers(token) :: multipart.headers.toList: _*)
      )
  }

  def createReaction(channelId: Snowflake, messageId: Snowflake, emoji: String): F[Unit] =
    client
      .expect[Unit](
        Request[F]()
          .withMethod(PUT)
          .withUri(apiEndpoint.addPath(s"channels/$channelId/messages/$messageId/reactions/$emoji/@me"))
          .withHeaders(headers(token))
      )
      .handleErrorWith(_ => Applicative[F].unit) // Throws: java.io.IOException: unexpected content length header with 204 response

  def addEmoji(guildId: Snowflake, name: String, emojiData: Array[Byte], roles: List[Snowflake] = Nil): F[Emoji] = {
    val imageData = "data:;base64," + fs2.Stream.emits(emojiData).through(fs2.text.base64.encode).compile.foldMonoid
    client
      .expect[Emoji](
        Request[F]()
          .withMethod(POST)
          .withUri(apiEndpoint.addPath(s"guilds/$guildId/emojis"))
          .withEntity(
            // TODO: Create case class for this
            Json.obj(
              "name"  -> name.asJson,
              "image" -> imageData.asJson,
              "roles" -> roles.asJson
            )
          )
          .withHeaders(headers(token))
      )
  }

  def listEmojis(guildId: Snowflake): F[List[Emoji]] =
    client
      .expect[List[Emoji]](
        Request[F]()
          .withMethod(GET)
          .withUri(apiEndpoint.addPath(s"guilds/$guildId/emojis"))
          .withHeaders(headers(token))
      )

  def getChannelMessage(channelId: Snowflake, messageId: Snowflake): F[Message] =
    client
      .expect[Message](
        Request[F]()
          .withMethod(GET)
          .withUri(apiEndpoint.addPath(s"channels/$channelId/messages/$messageId"))
          .withHeaders(headers(token))
      )

  def createWebhook(name: String, avatar: Option[ImageDataUri], channelId: Snowflake): F[Webhook] =
    client
      .expect[Webhook](
        Request[F]()
          .withMethod(POST)
          .withUri(apiEndpoint.addPath(s"channels/$channelId/webhooks"))
          .withEntity( // TODO Case class here
            Json.obj(
              "name"   -> name.asJson,
              "avatar" -> avatar.asJson
            )
          )
          .withHeaders(headers(token))
      )

  def getChannelWebhooks(channelId: Snowflake): F[List[Webhook]] =
    client
      .expect[List[Webhook]](
        Request[F]()
          .withMethod(GET)
          .withUri(apiEndpoint.addPath(s"channels/$channelId/webhooks"))
          .withHeaders(headers(token))
      )

  def getGuildWebhooks(guildId: Snowflake): F[List[Webhook]] =
    client
      .expect[List[Webhook]](
        Request[F]()
          .withMethod(GET)
          .withUri(apiEndpoint.addPath(s"guilds/$guildId/webhooks"))
          .withHeaders(headers(token))
      )

  def getWebhook(webhookId: Snowflake): F[Webhook] =
    client
      .expect[Webhook](
        Request[F]()
          .withMethod(GET)
          .withUri(apiEndpoint.addPath(s"webhooks/$webhookId"))
          .withHeaders(headers(token))
      )

  def getWebhookWithToken(webhookId: Snowflake, token: String): F[Webhook] =
    client
      .expect[Webhook](
        Request[F]()
          .withMethod(GET)
          .withUri(apiEndpoint.addPath(s"webhooks/$webhookId/$token"))
      )

  def modifyWebhook(webhookId: Snowflake, name: Option[String], avatar: Option[ImageDataUri], channelId: Option[Snowflake]): F[Webhook] =
    client
      .expect[Webhook](
        Request[F]()
          .withMethod(PATCH)
          .withUri(apiEndpoint.addPath(s"webhooks/$webhookId"))
          .withEntity(
            // TODO Case class here
            Json.obj(
              "name"       -> name.asJson,
              "avatar"     -> avatar.asJson,
              "channel_id" -> channelId.asJson
            )
          )
          .withHeaders(headers(token))
      )

  def modifyWebhookWithToken(webhookId: Snowflake, name: Option[String], avatar: Option[ImageDataUri], channelId: Option[Snowflake], token: String): F[Webhook] =
    client
      .expect[Webhook](
        Request[F]()
          .withMethod(PATCH)
          .withUri(apiEndpoint.addPath(s"webhooks/$webhookId/$token"))
          .withEntity(
            // TODO Case class here
            Json.obj(
              "name"       -> name.asJson,
              "avatar"     -> avatar.asJson,
              "channel_id" -> channelId.asJson
            )
          )
      )

  def deleteWebhook(webhookId: Snowflake): F[Status] =
    client
      .status(
        Request[F]()
          .withMethod(DELETE)
          .withUri(apiEndpoint.addPath(s"webhooks/$webhookId"))
          .withHeaders(headers(token))
      )

  def deleteWebhookWithToken(webhookId: Snowflake, token: String): F[Status] =
    client
      .status(
        Request[F]()
          .withMethod(DELETE)
          .withUri(apiEndpoint.addPath(s"webhooks/$webhookId/$token"))
      )

  def executeWebhookWithResponse(webhook: Webhook, webhookMessage: WebhookMessage): F[Message] =
    client.expect[Message](createExecuteWebhookRequest(webhook, webhookMessage, wait = true))

  def executeWebhook(webhook: Webhook, webhookMessage: WebhookMessage): F[Status] =
    client.status(createExecuteWebhookRequest(webhook, webhookMessage, wait = false))

  // TODO: Handle uploading files which requires multipart/form-data
  private def createExecuteWebhookRequest(webhook: Webhook, webhookMessage: WebhookMessage, wait: Boolean): Request[F] =
    Request[F]()
      .withMethod(POST)
      .withUri(
        apiEndpoint
          .addPath(s"webhooks/${webhook.id}/${webhook.token.get}")
          .withQueryParam("wait", wait)
      )
      .withEntity(webhookMessage)
      .withHeaders(headers(token))

  def getGlobalCommands(applicationId: Snowflake): IO[List[ApplicationCommand]] =
    client
      .expect[List[ApplicationCommand]](
        GET(
          apiEndpoint
            .addPath(s"applications/$applicationId/commands"),
          headers(token)
        )
      )

  // TODO: Make a case class for the request params
  def createGlobalApplicationCommand(applicationId: Snowflake)(
      name: String,
      description: String,
      options: List[ApplicationCommandOption] = List.empty,
      defaultPermission: Boolean = true
  ): IO[ApplicationCommand] =
    client
      .expect[ApplicationCommand](
        POST(
          // TODO Case class here
          Json.obj(
            "name"               -> name.asJson,
            "description"        -> description.asJson,
            "options"            -> options.asJson,
            "default_permission" -> defaultPermission.asJson
          ),
          apiEndpoint
            .addPath(s"applications/$applicationId/commands"),
          headers(token)
        )
      )

  def getGlobalApplicationCommand(applicationId: Snowflake, commandId: Snowflake): IO[ApplicationCommand] =
    client
      .expect[ApplicationCommand](
        GET(
          apiEndpoint
            .addPath(s"applications/$applicationId/commands/$commandId"),
          headers(token)
        )
      )

  def editGlobalApplicationCommand(
      applicationId: Snowflake,
      commandId: Snowflake
  )(name: Option[String], description: Option[String], options: Option[List[ApplicationCommandOption]], defaultPermission: Option[Boolean]): IO[ApplicationCommand] =
    client
      .expect[ApplicationCommand](
        PATCH(
          // TODO Case class here
          Json.obj(
            "name"               -> name.asJson,
            "description"        -> description.asJson,
            "options"            -> options.asJson,
            "default_permission" -> defaultPermission.asJson
          ),
          apiEndpoint
            .addPath(s"applications/$applicationId/commands/$commandId"),
          headers(token)
        )
      )

  def deleteGlobalApplicationCommand(applicationId: Snowflake, commandId: Snowflake): IO[Status] =
    client
      .status(
        DELETE(
          apiEndpoint
            .addPath(s"applications/$applicationId/commands/$commandId"),
          headers(token)
        )
      )

  def getGuildApplicationCommands(applicationId: Snowflake, guildId: Snowflake): IO[List[ApplicationCommand]] =
    client
      .expect[List[ApplicationCommand]](
        GET(
          apiEndpoint
            .addPath(s"applications/$applicationId/guilds/$guildId/commands"),
          headers(token)
        )
      )

  def bulkOverwriteGlobalApplicationCommands(applicationId: Snowflake)(applicationCommands: List[ApplicationCommand]): IO[List[ApplicationCommand]] =
    client
      .expect[List[ApplicationCommand]](
        PUT(
          applicationCommands.asJson,
          apiEndpoint
            .addPath(s"applications/$applicationId/commands"),
          headers(token)
        )
      )

  def createGuildApplicationCommand(
      applicationId: Snowflake,
      guildId: Snowflake
  )(name: String, description: String, options: List[ApplicationCommandOption] = List.empty, defaultPermission: Boolean = true): IO[ApplicationCommand] =
    client
      .expect[ApplicationCommand](
        POST(
          // TODO Case class here
          Json.obj(
            "name"               -> name.asJson,
            "description"        -> description.asJson,
            "options"            -> options.asJson,
            "default_permission" -> defaultPermission.asJson
          ),
          apiEndpoint
            .addPath(s"applications/$applicationId/guilds/$guildId/commands"),
          headers(token)
        )
      )

  def getGuildApplicationCommand(applicationId: Snowflake, guildId: Snowflake, commandId: Snowflake): IO[ApplicationCommand] =
    client
      .expect[ApplicationCommand](
        GET(
          apiEndpoint
            .addPath(s"applications/$applicationId/guilds/$guildId/commands/$commandId"),
          headers(token)
        )
      )

  def editGuildApplicationCommand(
      applicationId: Snowflake,
      guildId: Snowflake,
      commandId: Snowflake
  )(name: Option[String], description: Option[String], options: Option[List[ApplicationCommandOption]], defaultPermission: Option[Boolean]): IO[ApplicationCommand] =
    client
      .expect[ApplicationCommand](
        PATCH(
          // TODO Case class here
          Json.obj(
            "name"               -> name.asJson,
            "description"        -> description.asJson,
            "options"            -> options.asJson,
            "default_permission" -> defaultPermission.asJson
          ),
          apiEndpoint
            .addPath(s"applications/$applicationId/guilds/$guildId/commands/$commandId"),
          headers(token)
        )
      )

  def deleteGuildApplicationCommand(applicationId: Snowflake, guildId: Snowflake, commandId: Snowflake): IO[Status] =
    client
      .status(
        DELETE(
          apiEndpoint
            .addPath(s"applications/$applicationId/guilds/$guildId/commands/$commandId"),
          headers(token)
        )
      )

  def bulkOverwriteGuildApplicationCommands(applicationId: Snowflake, guildId: Snowflake)(applicationCommands: List[ApplicationCommand]): IO[List[ApplicationCommand]] =
    client
      .expect[List[ApplicationCommand]](
        PUT(
          applicationCommands.asJson,
          apiEndpoint
            .addPath(s"applications/$applicationId/guilds/$guildId/commands"),
          headers(token)
        )
      )

  // TODO: Not sure about these... Gateway interactions
  def createInteractionResponse(): IO[Unit]         = ???
  def editOriginalInteractionResponse(): IO[Unit]   = ???
  def deleteOriginalInteractionResponse(): IO[Unit] = ???
  def createFollowupMessage(): IO[Unit]             = ???
  def editFollowupMessage(): IO[Unit]               = ???
  def deleteFollowupMessage(): IO[Unit]             = ???
  // -------------------------------------------------

  def getGuildApplicationCommandPermissions(applicationId: Snowflake, guildId: Snowflake): IO[List[GuildApplicationCommandPermissions]] =
    client
      .expect[List[GuildApplicationCommandPermissions]](
        GET(
          apiEndpoint
            .addPath(s"applications/$applicationId/guilds/$guildId/commands/permissions"),
          headers(token)
        )
      )

  def getApplicationCommandPermissions(applicationId: Snowflake, guildId: Snowflake, commandId: Snowflake): IO[GuildApplicationCommandPermissions] =
    client
      .expect[GuildApplicationCommandPermissions](
        GET(
          apiEndpoint
            .addPath(s"applications/$applicationId/guilds/$guildId/commands/$commandId/permissions"),
          headers(token)
        )
      )

  def editApplicationCommandPermissions(applicationId: Snowflake, guildId: Snowflake, commandId: Snowflake)(permissions: List[ApplicationCommandPermission]): IO[Status] =
    client
      .status(
        PUT(
          permissions.asJson,
          apiEndpoint
            .addPath(s"applications/$applicationId/guilds/$guildId/commands/$commandId/permissions"),
          headers(token)
        )
      )

  // TODO: It says it takes a "partial" GuildApplicationCommandPermissions object
  def batchEditApplicationCommandPermissions(applicationId: Snowflake, guildId: Snowflake)(permissions: List[GuildApplicationCommandPermissions]): IO[Status] =
    client
      .status(
        PUT(
          permissions.asJson,
          apiEndpoint
            .addPath(s"applications/$applicationId/guilds/$guildId/commands/permissions"),
          headers(token)
        )
      )

  // TODO: Add Slack and Github Webhooks
}

object DiscordClient {
  def make[F[_]: ConcurrentEffect](token: String)(implicit cs: ContextShift[F]): Resource[F, DiscordClient[F]] =
    Resource.eval(utils.javaClient.map(javaClient => new DiscordClient(token, JdkHttpClient[F](javaClient))))

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
