package discord

import App._
import cats.effect._
import cats.implicits._
import discord.model._
import skunk.codec.all._
import skunk.implicits._
import skunk.Session

class App(client: DiscordClient, pool: ConnectionPool, wz: Warzone) {

  def handleEvent(event: DispatchEvent): IO[Unit] = event match {
    case DispatchEvent.MessageCreate(message) =>
      message.content match {
        case s"!wz $command" => handleCommand(command, message)
        case _               => IO.unit
      }
    case _ =>
      IO.unit
  }

  private def handleCommand(command: String, message: Message): IO[Unit] = command match {
    case "ping" =>
      val embed = Embed.make.withTitle("My Title").withDescription("My Description")
      client.sendEmbed(embed, message.channelId).void
    case "save" =>
      pool.use(_.prepare(registerUser).use(_.execute(message.author.username ~ message.channelId))).void
    case "stats" =>
      def embed(placement: String) = Embed.make.withTitle("Your Stats").withDescription(s"Placement: $placement")
      wz.checkMatchStatsForUser("Billzabob#1574").flatMap(placement => client.sendEmbed(embed(placement.getOrElse("N/A")), message.channelId)).void
    case unknown =>
      client.sendMessage(s"Unknown command: $unknown", message.channelId).void
  }
}

object App {
  type ConnectionPool = Resource[IO, Session[IO]]

  val registerUser =
    sql"""
      INSERT INTO registered_users
      VALUES (DEFAULT, $varchar, $varchar)
    """.command
}
