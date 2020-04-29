package discord.model.embed

import io.circe.Encoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import org.http4s.circe._
import org.http4s.Uri

case class Provider(name: Option[String], url: Option[Uri])

object Provider {
  implicit val config: Configuration              = Configuration.default.withSnakeCaseMemberNames
  implicit val providerEncoder: Encoder[Provider] = deriveConfiguredEncoder
}
