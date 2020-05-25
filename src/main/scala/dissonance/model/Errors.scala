package dissonance.model

import scala.util.control.NoStackTrace

object Errors {
  case class ConnectionClosedWithError(status: Int, reason: String) extends Throwable(s"status: $status, reason: $reason") with NoStackTrace
  case object NoHeartbeatAck                                        extends Throwable with NoStackTrace
}
