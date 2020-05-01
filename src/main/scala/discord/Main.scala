package discord

import cats.effect.{ExitCode, IO, IOApp}
import discord.model.{DispatchEvent, Message}
import discord.model.DispatchEvent.MessageCreate
import java.net.http.HttpClient
import org.http4s.client.jdkhttpclient.{JdkHttpClient, JdkWSClient}

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val token = args.head
    clients
      .flatMap {
        case (client, wsClient) =>
          val discordClient = new DiscordClient(client, token)
          val discordEvents = new Discord(token, client, wsClient)

          discordEvents.start(handleEvents(discordClient))
      }
      .as(ExitCode.Success)
  }

  def handleEvents(discordClient: DiscordClient)(event: DispatchEvent): IO[Unit] = event match {
    case MessageCreate(Message(channelId, "ping", _)) => discordClient.sendMessage("pong", channelId).void
    case _                                            => IO.unit
  }

  val clients =
    IO(HttpClient.newHttpClient).map(client => (JdkHttpClient[IO](client), JdkWSClient[IO](client)))

}
