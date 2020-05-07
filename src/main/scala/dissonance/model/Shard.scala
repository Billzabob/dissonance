package dissonance.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.Configuration

case class Shard(
    shardId: Int,
    numShards: Int
)

object Shard {
  implicit val config: Configuration        = Configuration.default.withSnakeCaseMemberNames
  implicit val shardDecoder: Decoder[Shard] = Decoder[(Int, Int)].map(shard => Shard(shard._1, shard._2))
  implicit val shardEncoder: Encoder[Shard] = Encoder[(Int, Int)].contramap(shard => (shard.shardId, shard.numShards))
}
