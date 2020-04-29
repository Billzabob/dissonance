package discord.model.user

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import io.circe.parser._
import discord.model.user.PremiumType.NitroClassic
import discord.model.user.UserRole.HouseBravery
import cats.implicits._
import discord.model.DiscordId

class UserSpec extends AnyFlatSpec with Matchers {
    "a user json" should "be parsed correctly" in {
        val rawJson = """{
                            "id": "80351110224678912",
                            "username": "Nelly",
                            "discriminator": "1337",
                            "avatar": "8342729096ea3675442027381ff50dfe",
                            "verified": true,
                            "email": "nelly@discordapp.com",
                            "flags": 64,
                            "premium_type": 1,
                            "public_flags": 64
                        }"""
        val expectedUser = User(
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

        parse(rawJson).flatMap(_.as[User]) mustBe Right(expectedUser)
    }

    "a full user json" should "be parsed correctly" in {
        val rawJson = """{
                    "id": "80351110224678912",
                    "username": "Nelly",
                    "discriminator": "1337",
                    "avatar": "8342729096ea3675442027381ff50dfe",
                    "bot": false,
                    "system": false,
                    "mfa_enabled": true,
                    "locale": "en",
                    "verified": true,
                    "email": "nelly@discordapp.com",
                    "flags": 64,
                    "premium_type": 1,
                    "public_flags": 64
                }"""

        val expectedUser = User(
            id = DiscordId(80351110224678912L),
            username = "Nelly",
            discriminator = "1337",
            avatar = "8342729096ea3675442027381ff50dfe".some,
            bot = false.some,
            system = false.some,
            mfaEnabled = true.some,
            locale = "en".some,
            verified = true.some,
            email = "nelly@discordapp.com".some,
            flags = List(HouseBravery),
            premiumType = NitroClassic.some,
            publicFlags = List(HouseBravery)
        )

        parse(rawJson).flatMap(_.as[User]) mustBe Right(expectedUser)
    }
}