package dissonance.data

import cats.syntax.all._
import dissonance.TestUtils._
import io.circe.parser._
import weaver.SimpleIOSuite

object DispatchSpec extends SimpleIOSuite {

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
  test("dispatch events should parse correctly") {
    for {
      rawJson <- readFileFromResource("/models/events.ndjson").compile.toList
      expectations = rawJson.map { event =>
                       expect(parse(event).flatMap(_.as[ControlMessage]).isRight)
                     }
    } yield expectations.combineAll
  }
}
