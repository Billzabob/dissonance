package dissonance.model.channel

import cats.data.NonEmptyList
import cats.implicits._
import dissonance.model.Snowflake
import io.circe.Encoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class ModifyChannel(
    name: Option[String],
    `type`: Option[ChannelType],
    position: Option[Int],
    topic: Option[String],
    nsfw: Option[Boolean],
    rateLimitPerUser: Option[Int],
    bitrate: Option[Int],
    userLimit: Option[Int],
    permissionOverwrites: Option[NonEmptyList[Overwrite]],
    parentId: Option[Snowflake]
) {
  def withName(name: String)                      = copy(name = name.some)
  def withType(`type`: ChannelType)               = copy(`type` = `type`.some)
  def withPosition(position: Int)                 = copy(position = position.some)
  def withTopic(topic: String)                    = copy(topic = topic.some)
  def withNsfw(nsfw: Boolean)                     = copy(nsfw = nsfw.some)
  def withRateLimitPerUser(rateLimitPerUser: Int) = copy(rateLimitPerUser = rateLimitPerUser.some)
  def withBitrate(bitrate: Int)                   = copy(bitrate = bitrate.some)
  def withUserLimit(userLimit: Int)               = copy(userLimit = userLimit.some)
  def withPermissionOverwrite(overwrite: Overwrite) =
    copy(permissionOverwrites = permissionOverwrites match {
      case Some(overwrites) => overwrites.prepend(overwrite).some
      case None             => NonEmptyList.one(overwrite).some
    })
  def withParentId(parentId: Snowflake) = copy(parentId = parentId.some)
}

object ModifyChannel {
  def make = ModifyChannel(None, None, None, None, None, None, None, None, None, None)

  implicit val config: Configuration                        = Configuration.default.withSnakeCaseMemberNames
  implicit val modifyChannelEncoder: Encoder[ModifyChannel] = deriveConfiguredEncoder
}
