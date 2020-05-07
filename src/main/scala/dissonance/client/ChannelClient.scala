package dissonance.client

import cats.effect._
import dissonance.Discord._
import dissonance.model.channel._
import dissonance.model.embed.Embed
import dissonance.model.message._
import dissonance.model.Snowflake
import dissonance.model.user.User
import fs2.Stream
import io.circe.Json
import io.circe.syntax._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.client.dsl.io._
import org.http4s.Method._

class ChannelClient(token: String, client: Client[IO]) {

  def get(channelId: Snowflake): IO[Channel] =
    client
      .expect[Channel](
        GET(
          apiEndpoint.addPath(s"channels/$channelId"),
          headers(token)
        )
      )

  def sendMessage(message: String, channelId: Snowflake, tts: Boolean = false): IO[Message] =
    client
      .expect[Message](
        POST(
          Json.obj(
            "content" -> message.asJson,
            "tts"     -> tts.asJson
          ),
          apiEndpoint.addPath(s"channels/$channelId/messages"),
          headers(token)
        )
      )

  def sendEmbed(embed: Embed, channelId: Snowflake): IO[Message] =
    client
      .expect[Message](
        POST(
          Json.obj("embed" -> embed.asJson),
          apiEndpoint.addPath(s"channels/$channelId/messages"),
          headers(token)
        )
      )

  def modify(channelId: Snowflake, modification: ModifyChannel): IO[Channel] =
    client
      .expect[Channel](
        PATCH(
          modification.asJson,
          apiEndpoint.addPath(s"channels/$channelId"),
          headers(token)
        )
      )

  def delete(channelId: Snowflake): IO[Channel] =
    client
      .expect[Channel](
        DELETE(
          apiEndpoint.addPath(s"channels/$channelId"),
          headers(token)
        )
      )

  // TODO: Return Stream[IO, Message] instead
  def getMessages(channelId: Snowflake, count: Int): IO[List[Message]] =
    client
      .expect[List[Message]](
        GET.apply(
          apiEndpoint.addPath(s"channels/$channelId/messages").withQueryParam("limit", count),
          headers(token)
        )
      )

  def getMessage(channelId: Snowflake, messageId: Snowflake): IO[Message] =
    client
      .expect[Message](
        GET.apply(
          apiEndpoint.addPath(s"channels/$channelId/messages/$messageId"),
          headers(token)
        )
      )

  def addReaction(channelId: Snowflake, messageId: Snowflake, emoji: String): IO[Unit] = ???

  def getReactions(channelId: Snowflake, messageId: Snowflake, emoji: String): Stream[IO, User] = ???

  def deleteOwnReaction(channelId: Snowflake, messageId: Snowflake, emoji: String): IO[Unit] = ???

  def deleteUserReaction(channelId: Snowflake, messageId: Snowflake, emoji: String, userId: Snowflake): IO[Unit] = ???

  def deleteAllReactions(channelId: Snowflake, messageId: Snowflake): IO[Unit] = ???

  def deleteAllReactionsForEmoji(channelId: Snowflake, messageId: Snowflake, emoji: String): IO[Unit] = ???

  def editMessage(channelId: Snowflake, messageId: Snowflake, messageEdit: MessageEdit): IO[Message] = ???

  // And many more...
}
