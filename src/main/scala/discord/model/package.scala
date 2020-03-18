package discord

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import io.estatico.newtype.macros.newtype

package object model {
  @newtype case class EventCount(count: Long Refined Positive)
}
