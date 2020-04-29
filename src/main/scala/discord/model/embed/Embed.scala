package discord.model.embed

import cats.data.NonEmptyList
import cats.implicits._
import io.circe.Encoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import java.time.Instant
import org.http4s.circe._
import org.http4s.Uri


case class Embed(
    title: Option[String],
    `type`: Option[EmbedType],
    description: Option[String],
    url: Option[Uri],
    timestamp: Option[Instant],
    color: Option[Color],
    footer: Option[Footer],
    image: Option[Image],
    thumbnail: Option[Image],
    video: Option[Video],
    provider: Option[Provider],
    author: Option[Author],
    fields: Option[NonEmptyList[Field]]
) {
  def withTitle(title: String)             = copy(title = title.some)
  def withType(`type`: EmbedType)          = copy(`type` = `type`.some)
  def withDescription(description: String) = copy(description = description.some)
  def withUrl(url: Uri)                    = copy(url = url.some)
  def withTimestamp(timestamp: Instant)    = copy(timestamp = timestamp.some)
  def withColor(color: Color)              = copy(color = color.some)
  def withFooter(footer: Footer)           = copy(footer = footer.some)
  def withImage(image: Image)              = copy(image = image.some)
  def withThumbnail(thumbnail: Image)      = copy(thumbnail = thumbnail.some)
  def withVideo(video: Video)              = copy(video = video.some)
  def withProvider(provider: Provider)     = copy(provider = provider.some)
  def withAuthor(author: Author)           = copy(author = author.some)
  def addField(field: Field) =
    copy(fields = fields match {
      case Some(fields) => fields.prepend(field).some
      case None         => NonEmptyList.one(field).some
    })
}

object Embed {
  def make = Embed(None, None, None, None, None, None, None, None, None, None, None, None, None)

  implicit val config: Configuration        = Configuration.default.withSnakeCaseMemberNames
  implicit val embedEncoder: Encoder[Embed] = deriveConfiguredEncoder
}
