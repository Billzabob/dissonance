package dissonance.data

import scala.util.control.NoStackTrace

object Errors {
  case object NoHeartbeatAck extends Throwable with NoStackTrace
}
