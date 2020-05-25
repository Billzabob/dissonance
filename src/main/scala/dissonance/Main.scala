package dissonance

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import dissonance.DiscordClient.WebhookMessage
import dissonance.model.{Event, Snowflake}
import dissonance.model.Event.MessageCreate
import dissonance.model.intents.Intent
import dissonance.model.message.BasicMessage
import org.http4s.implicits._

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val token = args.head
    Discord.make(token).use(discord => discord.subscribe(Intent.GuildMessages).evalMap(handleEvents(discord.client)).compile.drain).as(ExitCode.Success)
  }

  def handleEvents(discordClient: DiscordClient): Event => IO[Unit] = {
    case MessageCreate(BasicMessage("ping", _, channelId))    => discordClient.sendMessage("pong", channelId).void
    case MessageCreate(BasicMessage("webhook", _, channelId)) => testWebhookMethods(discordClient, channelId).void
    case _                                                    => IO.unit
  }

  private def testWebhookMethods(discordClient: DiscordClient, channelId: Snowflake) =
    for {
      webhook <- discordClient.createWebhook(
                   "ShoganWebhookTest",
                   uri"https://banner2.cleanpng.com/20180504/waq/kisspng-webhook-computer-icons-discord-application-program-5aebec8beabb08.4201354315254109559615.jpg".some,
                   channelId
                 )
      sendStatus <- discordClient.executeWebhook(
                      webhook,
                      None,
                      WebhookMessage(
                        "test content",
                        "test username".some,
                        None,
                        None,
                        (),
                        List.empty,
                        (),
                        ()
                      )
                    )
      _ <- discordClient.deleteWebhook(webhook.id)
    } yield sendStatus
}
