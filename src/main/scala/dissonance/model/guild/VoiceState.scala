package dissonance.model.guild

import dissonance.model.Event.Snowflake
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class VoiceState(
    guildId: Snowflake,
    channelId: Option[Snowflake],
    userId: Snowflake,
    member: Option[Member],
    sessionId: String,
    deaf: Boolean,
    mute: Boolean,
    selfDeaf: Boolean,
    selfMute: Boolean,
    selfStream: Option[Boolean],
    suppress: Boolean
)

object VoiceState {
  implicit val config: Configuration                 = Configuration.default.withSnakeCaseMemberNames
  implicit val voidStateDecoder: Decoder[VoiceState] = deriveConfiguredDecoder
}
