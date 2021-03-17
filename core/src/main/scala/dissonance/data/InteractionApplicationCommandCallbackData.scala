package dissonance.data

import cats.syntax.all._
import io.circe.{Encoder, Json}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

case class InteractionApplicationCommandCallbackData(
    tts: Option[Boolean],
    content: String,
    embeds: Option[List[Embed]],
    allowedMentions: Option[Json],
    flags: Option[List[InteractionCallbackFlag]]
) {
  def withContent(content: String) = copy(content = content)
  def addEmbed(embed: Embed) =
    copy(embeds = embeds match {
      case Some(embeds) => (embeds :+ embed).some
      case None         => List(embed).some
    })
  def addEmbeds(newEmbeds: Embed*) =
    copy(embeds = embeds match {
      case Some(embeds) => (embeds ++ newEmbeds.toList).some
      case None         => newEmbeds.toList.some
    })
  def addFlag(flag: InteractionCallbackFlag) =
    copy(flags = flags match {
      case Some(flags) => (flags :+ flag).some
      case None        => List(flag).some
    })
  def addFlags(newFlags: InteractionCallbackFlag*) =
    copy(flags = flags match {
      case Some(flags) => (flags ++ newFlags.toList).some
      case None        => newFlags.toList.some
    })
}

object InteractionApplicationCommandCallbackData {
  def make = InteractionApplicationCommandCallbackData(None, "", None, None, None)

  implicit val config: Configuration                                                                                = Configuration.default.withSnakeCaseMemberNames
  implicit val interactionApplicationCommandCallbackDataEncoder: Encoder[InteractionApplicationCommandCallbackData] = deriveConfiguredEncoder
}
