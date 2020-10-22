package dissonance.data

import cats.syntax.all._
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

sealed trait Update extends Product with Serializable

object Update {
  case class Edit(message: Message) extends Update
  // Every time an embed is created, this event is generated immediately after the MESSAGE_CREATE
  case class EmbedUpdate(
      id: Snowflake,
      channelId: Snowflake,
      guildId: Snowflake,
      embeds: List[Embed]
  ) extends Update

  implicit val config: Configuration          = Configuration.default.withSnakeCaseMemberNames
  implicit val updateDecoder: Decoder[Update] = Decoder[Message].map(Edit).widen[Update] or deriveConfiguredDecoder[EmbedUpdate].widen[Update]
}
