<img align="right" src="https://github.com/Billzabob/dissonance/blob/master/core/src/main/resources/DissonanceLogo.png" height="150px" style="padding-left: 20px"/>


[![](https://github.com/Billzabob/discord/workflows/build/badge.svg)](https://github.com/Billzabob/dissonance)
[![](https://img.shields.io/discord/390751088829005826.svg?style=flat)](https://discordapp.com/invite/JXt4Zd)
[![](https://codecov.io/gh/Billzabob/dissonance/branch/master/graph/badge.svg)](https://codecov.io/gh/Billzabob/dissonance)

# Dissonance

A Discord framework for Scala

```scala
import cats.effect.{ExitCode, IO, IOApp}
import dissonance.data.events.MessageCreate
import dissonance.data.intents.Intent
import dissonance.data.message.BasicMessage
import dissonance.data.Shard
import dissonance.Discord

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    Discord.make(args.head).use { discord =>
      discord
        .subscribe(Shard.singleton, Intent.GuildMessages)
        .evalMap {
          case MessageCreate(BasicMessage(_, "ping", _, channelId)) =>
            discord.client.sendMessage("pong", channelId).void
          case _ => IO.unit
        }
        .compile
        .drain
        .as(ExitCode.Success)
    }
}
```