package dissonance

import cats.effect.{ExitCode, IO, IOApp}
import dissonance.model.Event
import dissonance.model.Event.MessageCreate
import dissonance.model.message.Message

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val token = args.head
    Discord.make(token).use(discord => discord.subscribe.evalMap(handleEvents(discord.client)).compile.drain).as(ExitCode.Success)
  }

  def handleEvents(discordClient: DiscordClient): Event => IO[Unit] = {
    case MessageCreate(Message("ping", channelId, _)) => discordClient.sendMessage("pong", channelId).void
    case _                                            => IO.unit
  }
}
