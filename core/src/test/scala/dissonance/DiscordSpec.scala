package dissonance

import cats.effect._
import cats.effect.kernel.Deferred
import cats.effect.std.Queue
import cats.syntax.all._
import dissonance.data._
import dissonance.data.events.{MessageCreate, Ready}
import org.http4s.Uri
import weaver._

import scala.concurrent.duration._

object DiscordSpec extends IOSuite {
  type EventQueue = Queue[IO, Event]
  type SessionId  = String

  override type Res = Discord[IO]

  override def sharedResource: Resource[IO, Res] =
    for {
      token <- Resource.eval(
                 ciris
                   .env("DISSONANCE_IT_TOKEN")
                   .secret
                   .load[IO]
               )
      discord <- Discord.make(token.value)
    } yield discord

  private val testChannelId: Snowflake = 834670988083855370L

  def withBackgroundProcessing(discord: Discord[IO])(runExpectation: (SessionId, EventQueue) => IO[Expectations]): IO[Expectations] =
    Resource
      .eval((Deferred[IO, SessionId], Queue.bounded[IO, Event](100)).tupled)
      .use { case (sessionId, queue) =>
        val backgroundConsumer = discord
          .subscribe(Shard.singleton, Intent.GuildMessages)
          .evalMap {
            case Ready(_, _, sid, _) =>
              sessionId.complete(sid)
            case e =>
              queue.offer(e)
          }
          .compile
          .drain

        backgroundConsumer.background.use { _ =>
          for {
            sessionId   <- sessionId.get
            expectation <- runExpectation(sessionId, queue)
          } yield expectation
        }
      }

  test("sendMessage should create MessageCreate event") { discord =>
    withBackgroundProcessing(discord) { case (_, queue) =>
      val pingMessage = "ping"
      for {
        _ <- discord.client.sendMessage(pingMessage, testChannelId)
        expectation <- queue.take
                         .map {
                           case MessageCreate(message) =>
                             expect(message.content == pingMessage)
                           case e =>
                             failure(s"Received $e instead of MessageCreate")
                         }
                         .timeout(10.seconds)
      } yield expectation
    }
  }

  test("sendEmbed (simple) should create MessageCreate event with Embed") { discord =>
    withBackgroundProcessing(discord) { case (_, queue) =>
      val title        = "Matt"
      val description  = "Radar Technician"
      val thumbnailUrl = "https://media.giphy.com/media/tywn2kxZC91lK/giphy.gif"
      val image        = Image(Uri.fromString(thumbnailUrl).toOption, None, None, None)
      val footer       = Footer("*actually Kylo Ren", Uri.fromString("https://i.kym-cdn.com/entries/icons/original/000/025/003/benswoll.jpg").toOption, None)
      val embeddedMessage = Embed.make
        .withTitle(title)
        .withDescription(description)
        .withThumbnail(image)
        .withFooter(footer)

      for {
        _ <- discord.client.sendEmbed(embeddedMessage, testChannelId)
        expectation <- queue.take
                         .map {
                           case MessageCreate(message) =>
                             val actualEmbed = message.embeds.head
                             expect.all(
                               actualEmbed.title.contains(title),
                               actualEmbed.description.contains(description),
                               actualEmbed.thumbnail.flatMap(_.url.map(_.renderString)).contains(thumbnailUrl),
                               actualEmbed.footer.map(_.text).contains(footer.text)
                             )
                           case e =>
                             failure(s"Received $e instead of MessageCreate")
                         }
                         .timeout(10.seconds)
      } yield expectation
    }
  }

}
