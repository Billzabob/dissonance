package dissonance

import java.nio.file.Path

import cats.effect.IO
import fs2.io.file.Files
import fs2.text

object TestUtils {
  def readFileFromResource(path: String): fs2.Stream[IO, String] = {
    Files[IO]
      .readAll(Path.of(getClass.getResource(path).toURI), 4096)
      .through(text.utf8.decode)
      .through(fs2.text.lines)
      .dropLastIf(_.isEmpty)
  }
}
