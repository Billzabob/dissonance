package discord

import cats.effect._
import cats.implicits._
import discord.DB._
import discord.model._
import natchez.Trace.Implicits.noop
import skunk._
import skunk.codec.all._
import skunk.implicits._

class DB(pool: ConnectionPool) {

  def registerUser(discordId: DiscordId, accountName: AccountName): IO[RegisterResult] = pool.use { s =>
    s.prepare(getUserAccountQuery).use(_.option(discordId)).flatMap {
      case Some(existingAccount) if existingAccount != accountName =>
        AlreadyRegistered(existingAccount).pure[IO]
      case Some(_) =>
        RegisterSuccess(discordId).pure[IO]
      case None =>
        s.prepare(registerUserQuery).use(_.unique(discordId ~ accountName)).map(RegisterSuccess)
    }
  }

  def updateAccountName(discordId: DiscordId, accountName: AccountName): IO[UpdateAccountResult] = pool.use { s =>
    s.prepare(updateAccountQuery).use(_.option(accountName ~ discordId)).map {
      case Some(discordId) =>
        UpdateAccountSuccess(discordId)
      case None =>
        NoExistingAccount
    }
  }

  def unregisterUser(discordId: DiscordId): IO[Unit] = pool.use(_.prepare(unregisterUserCommand).use(_.execute(discordId))).void
}

object DB {
  type ConnectionPool = Resource[IO, Session[IO]]

  def pool(implicit c: Concurrent[IO], cs: ContextShift[IO]): Resource[IO, ConnectionPool] =
    Session.pooled(
      host = "localhost",
      port = 5432,
      user = "nhallstrom",
      database = "public",
      password = None,
      max = 10
    )

  sealed trait RegisterResult
  case class RegisterSuccess(id: DiscordId)       extends RegisterResult
  case class AlreadyRegistered(name: AccountName) extends RegisterResult

  sealed trait UpdateAccountResult
  case class UpdateAccountSuccess(id: DiscordId) extends UpdateAccountResult
  case object NoExistingAccount                  extends UpdateAccountResult

  val discordId: Codec[DiscordId]     = int8.imap(DiscordId.apply)(_.value)
  val accountName: Codec[AccountName] = varchar.imap(AccountName.apply)(_.value)

  val registerUserQuery =
    sql"""
      INSERT INTO users
      VALUES ($discordId, $accountName)
      RETURNING discord_id
    """.query(discordId)

  val getUserAccountQuery =
    sql"""
      SELECT account_name
      FROM users
      WHERE discord_id = $discordId
    """.query(accountName)

  val updateAccountQuery =
    sql"""
      UPDATE users
      SET account_name = $accountName
      WHERE discord_id = $discordId
      RETURNING discord_id
    """.query(discordId)

  val unregisterUserCommand =
    sql"""
        DELETE FROM users
        WHERE discord_id = $discordId
      """.command
}
