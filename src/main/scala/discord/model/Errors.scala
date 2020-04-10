package discord.model

// import scala.util.control.NoStackTrace

object Errors {
  case class ConnectionClosedWithError(statusCode: Int, reason: String) extends Throwable
  case class SessionInvalid(resumable: Boolean)                         extends Throwable
  case object NoHeartbeatAck                                            extends Throwable
  case object ReconnectReceived                                         extends Throwable
}
