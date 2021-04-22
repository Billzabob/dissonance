package dissonance.data

import cats.syntax.all._
import dissonance.TestUtils._
import dissonance.data.PremiumType.{None => _, _}
import dissonance.data.UserRole._
import io.circe.parser._
import weaver.SimpleIOSuite

object UserSpec extends SimpleIOSuite {
  val baseUser: User = User(
    id = DiscordId(80351110224678912L),
    username = "Nelly",
    discriminator = "1337",
    avatar = "8342729096ea3675442027381ff50dfe".some,
    bot = None,
    system = None,
    mfaEnabled = None,
    locale = None,
    verified = true.some,
    email = "nelly@discordapp.com".some,
    flags = List(HouseBravery),
    premiumType = NitroClassic.some,
    publicFlags = List(HouseBravery)
  )

  test("a partial user json should be parsed correctly") {
    for {
      rawJson     <- readFileFromResource("/models/partialUser.json").compile.toList.map(_.mkString("\n"))
      expectedUser = baseUser
      expectations = parse(rawJson).flatMap(_.as[User]).map(parsedUser => expect(parsedUser == expectedUser))
    } yield expectations.combineAll
  }

  test("a full user json should be parsed correctly") {
    for {
      rawJson <- readFileFromResource("/models/fullUser.json").compile.toList.map(_.mkString("\n"))
      expectedUser = baseUser.copy(
                       bot = false.some,
                       system = false.some,
                       mfaEnabled = true.some,
                       locale = "en".some
                     )
      expectations = parse(rawJson).flatMap(_.as[User]).map(parsedUser => expect(parsedUser == expectedUser))
    } yield expectations.combineAll
  }
}
