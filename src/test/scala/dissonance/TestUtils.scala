package dissonance

import scala.io.Source
import cats.effect.IO
import cats.effect.ContextShift

object TestUtils {
    def readFileFromResource(path: String)(implicit contextShift: ContextShift[IO]) = {
        val acquire = IO.shift *> IO(Source.fromURL(getClass.getResource(path)))
        
        acquire.bracket { in =>
            IO(in.getLines().mkString("\n"))
        } { in =>
            IO(in.close()).void
        }
    }
}