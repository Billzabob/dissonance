package dissonance.model

import scala.util.control.NoStackTrace

object Errors {
  case class SessionInvalid(resumable: Boolean) extends Throwable(s"resumable: $resumable") with NoStackTrace
  case class ConnectionClosedWithError(status: Int, reason: String) extends Throwable(s"status: $status, reason: $reason") with NoStackTrace
  case object NoHeartbeatAck                    extends Throwable with NoStackTrace
  case object ReconnectReceived                 extends Throwable with NoStackTrace
}
