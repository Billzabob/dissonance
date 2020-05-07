package dissonance.model

import cats.effect._
import dissonance.TestUtils._
import io.circe.parser._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import scala.concurrent.ExecutionContext

class DispatchSpec extends AnyFlatSpec with Matchers {
  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  "dispatch events" should "parse correctly" in {
    // TODO: Still need to generate the following events somehow, and probably more examples of the existing ones
    // RESUMED
    // GUILD_DELETE
    // GUILD_INTEGRATIONS_UPDATE
    // GUILD_MEMBER_ADD
    // GUILD_MEMBER_REMOVE
    // GUILD_MEMBER_UPDATE
    // GUILD_MEMBERS_CHUNK
    // INVITE_CREATE
    // INVITE_DELETE
    // MESSAGE_DELETE_BULK
    // MESSAGE_REACTION_REMOVE_ALL
    // MESSAGE_REACTION_REMOVE_EMOJI
    // PRESENCE_UPDATE
    // USER_UPDATE
    // VOICE_SERVER_UPDATE

    val rawJson = readFileFromResource("/models/events.ndjson").unsafeRunSync()

    rawJson.foreach { event =>
      parse(event).flatMap(_.as[ControlMessage]) must matchPattern { case Right(_) => }
    }
  }
}
