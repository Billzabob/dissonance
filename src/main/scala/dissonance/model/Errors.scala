package dissonance.model

import scala.util.control.NoStackTrace

object Errors {
  case class SessionInvalid(resumable: Boolean) extends NoStackTrace
  case object NoHeartbeatAck                    extends NoStackTrace
  case object ReconnectReceived                 extends NoStackTrace
}
