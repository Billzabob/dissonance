package dissonance.data

import cats.effect._
import cats.syntax.all._
import dissonance.data.PremiumType.{None => _, _}
import dissonance.data.UserRole._
import dissonance.TestUtils._
import io.circe.parser._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import scala.concurrent.ExecutionContext

class UserSpec extends AnyFlatSpec with Matchers {
  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val baseUser = User(
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

  "a partial user json" should "be parsed correctly" in {
    val rawJson = readFileFromResource("/models/partialUser.json").unsafeRunSync().mkString("\n")

    val expectedUser = baseUser

    parse(rawJson).flatMap(_.as[User]) mustBe Right(expectedUser)
  }

  "a full user json" should "be parsed correctly" in {
    val rawJson = readFileFromResource("/models/fullUser.json").unsafeRunSync().mkString("\n")

    val expectedUser = baseUser.copy(
      bot = false.some,
      system = false.some,
      mfaEnabled = true.some,
      locale = "en".some
    )

    parse(rawJson).flatMap(_.as[User]) mustBe Right(expectedUser)
  }
}
