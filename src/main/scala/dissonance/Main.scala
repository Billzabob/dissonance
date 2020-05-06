package dissonance

import cats.effect.{ExitCode, IO, IOApp}
import dissonance.model.Event
import dissonance.model.Event.MessageCreate
import dissonance.model.intents.Intent
import dissonance.model.message.BasicMessage

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val token = args.head
    Discord.make(token).use(discord => discord.subscribe(Intent.values.toList).evalMap(handleEvents(discord.client)).compile.drain).as(ExitCode.Success)
  }

  def handleEvents(discordClient: DiscordClient): Event => IO[Unit] = {
    case MessageCreate(BasicMessage("ping", _, channelId)) => discordClient.sendMessage("pong", channelId).void
    case _                                                 => IO.unit
  }
}
