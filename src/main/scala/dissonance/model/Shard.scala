package dissonance.model

import io.circe.Encoder
import io.circe.generic.extras.Configuration

case class Shard(
    shardId: Int,
    numShards: Int
)

object Shard {
  implicit val config: Configuration   = Configuration.default.withSnakeCaseMemberNames
  implicit val encoder: Encoder[Shard] = Encoder[(Int, Int)].contramap(shard => (shard.shardId, shard.numShards))
}
