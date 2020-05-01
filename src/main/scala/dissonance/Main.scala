package dissonance

import cats.effect.{ExitCode, IO, IOApp}
import dissonance.model.{DispatchEvent, Message}
import dissonance.model.DispatchEvent.MessageCreate

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val token = args.head
    Discord.make(token).use(discord => discord.subscribe(handleEvents(discord.client))).as(ExitCode.Success)
  }

  def handleEvents(discordClient: DiscordClient)(event: DispatchEvent): IO[Unit] = event match {
    case MessageCreate(Message(channelId, "ping", _)) => discordClient.sendMessage("pong", channelId).void
    case other                                        => utils.putStrLn(other.toString)
  }
}
