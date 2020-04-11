package discord

import cats.effect._
import cats.implicits._
import discord.DB._
import discord.model._

class App(client: DiscordClient, db: DB, wz: Warzone) {

  def handleEvent(event: DispatchEvent): IO[Unit] = event match {
    case DispatchEvent.MessageCreate(message) =>
      message.content match {
        case s"!wz $command" => handleCommand(command, message)
        case _               => IO.unit
      }
    case _ =>
      IO.unit
  }

  // Handle SQL errors
  private def handleCommand(command: String, message: Message): IO[Unit] = command match {
    case "ping" =>
      val embed = Embed.make.withTitle("My Title").withDescription("My Description")
      client.sendEmbed(embed, message.channelId).void
    case s"register $accountName" =>
      db.registerUser(message.author.id, AccountName(accountName)).flatMap {
        case RegisterSuccess(_) =>
          client.sendMessage(s"Successfully registered as $accountName", message.channelId).void
        case AlreadyRegistered(accountName) =>
          client.sendMessage(s"You are already registered with account name: $accountName, if you wish to change this, use the `change` command instead", message.channelId).void
      }
    case s"change $accountName" =>
      db.updateAccountName(message.author.id, AccountName(accountName)).flatMap {
        case UpdateAccountSuccess(_) =>
          client.sendMessage(s"Successfully changed registered account to $accountName", message.channelId).void
        case NoExistingAccount =>
          client.sendMessage("No pre-existing account detected, use `register` first", message.channelId).void
      }
    case "unregister" =>
      db.unregisterUser(message.author.id) >> client.sendMessage("Successfully unregistered", message.channelId).void
    case "stats" =>
      def embed(placement: String) = Embed.make.withTitle("Your Stats").withDescription(s"Placement: $placement")
      wz.checkMatchStatsForUser("Billzabob#1574").flatMap(placement => client.sendEmbed(embed(placement.getOrElse("N/A")), message.channelId)).void
    case unknown =>
      client.sendMessage(s"Unknown command: $unknown", message.channelId).void
  }
}
