package discord

import cats.effect._
import cats.effect.concurrent._
import cats.implicits._
import discord.Heartbeater._
import discord.model.Heartbeat
import discord.utils._
import fs2.Stream
import io.circe.syntax._
import org.http4s.client.jdkhttpclient.WSConnectionHighLevel
import org.http4s.client.jdkhttpclient.WSFrame._
import scala.concurrent.duration.FiniteDuration

class Heartbeater private (sequenceNumber: SequenceNumber, ackReceived: AckReceived, connectionDead: ConnectionDead) {
  def heartbeat(interval: FiniteDuration, connection: WSConnectionHighLevel[IO])(implicit t: Timer[IO]): Stream[IO, Unit] = {
    def sendHeartbeat: Stream[IO, Unit] = Stream.eval(ackReceived.getAndSet(false)).flatMap {
      case true =>
        Stream.eval(makeHeartbeat.flatMap(connection.send)) ++ sendHeartbeat
      case false =>
        Stream.eval(putStrLn[IO]("No heartbeat! Connection has died!") >> connectionDead.complete(()))
    }
    sendHeartbeat.metered(interval)
  }

  def receivedAck =
    ackReceived.set(true)

  def updateSequenceNumber(nextSequenceNumber: Int) =
    sequenceNumber.set(nextSequenceNumber.some)

  def flatlined =
    connectionDead.get.map(_.asRight[Throwable])

  private def makeHeartbeat =
    sequenceNumber.get.map(Heartbeat.apply).map(heartbeat => Text(heartbeat.asJson.noSpaces))
}

object Heartbeater {
  def make(implicit cs: ContextShift[IO]): IO[Heartbeater] =
    (Ref[IO].of(none[Int]), Ref[IO].of(true), Deferred[IO, Unit]).mapN((s, a, c) => new Heartbeater(s, a, c))

  type SequenceNumber = Ref[IO, Option[Int]]
  type AckReceived    = Ref[IO, Boolean]
  type ConnectionDead = Deferred[IO, Unit]
}
