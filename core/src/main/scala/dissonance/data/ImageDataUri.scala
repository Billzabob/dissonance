package dissonance.data

import io.circe.{Decoder, Encoder}

// TODO: Add Refined? for making sure that the Uri is in the correct format of
// data:image/IMAGE_TYPE;base64,BASE64_ENCODED_JPEG_IMAGE_DATA
case class ImageDataUri(value: String)

object ImageDataUri {
  implicit val encoder: Encoder[ImageDataUri] = Encoder[String].contramap(_.value)
  implicit val decoder: Decoder[ImageDataUri] = Decoder[String].map(ImageDataUri.apply)
}
