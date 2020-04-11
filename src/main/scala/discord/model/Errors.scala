package discord.model

import scala.util.control.NoStackTrace

object Errors {
  case class ConnectionClosedWithError(statusCode: Int, reason: String) extends NoStackTrace
  case class SessionInvalid(resumable: Boolean)                         extends NoStackTrace
  case object NoHeartbeatAck                                            extends NoStackTrace
  case object ReconnectReceived                                         extends NoStackTrace
}
