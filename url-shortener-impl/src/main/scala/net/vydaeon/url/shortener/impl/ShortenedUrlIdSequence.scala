package net.vydaeon.url.shortener.impl

import scala.collection.immutable

import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer.emptySingletonFormat
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry

import ShortenedUrlIdSequence.SHARD_COUNT
import play.api.libs.json.Json.format

object ShortenedUrlIdSequence {

  val SHARD_COUNT = 10
}

/**
 * Generates ID numbers (to be converted to short URL fragments);
 * each instance generates IDs starting with its (numerical) entity ID
 * and incrementing by the total number of instances (the shard count).
 */
class ShortenedUrlIdSequence extends PersistentEntity {

  override type State = SequenceState
  override type Command = SequenceCommand
  override type Event = SequenceEvent

  override def initialState = SequenceState(entityId.toLong)

  override def behavior = Actions()
    .onCommand[GetNextId.type, Long] {
      case (_: GetNextId.type, ctx, state) => getNextId(ctx, state)
    }
    .onEvent {
      case (_: NextIdGenerated.type, state) => nextIdGenerated(state)
    }

  private def getNextId(ctx: CommandContext[Long], state: State) =
    ctx.thenPersist(NextIdGenerated) { _ =>
      ctx.reply(state.nextId)
    }

  private def nextIdGenerated(state: State) = SequenceState(state.nextId + SHARD_COUNT)
}

final case class SequenceState(nextId: Long)

sealed trait SequenceCommand

final case object GetNextId extends SequenceCommand with ReplyType[Long]

sealed trait SequenceEvent

final case object NextIdGenerated extends SequenceEvent

object SequenceSerializerRegistry extends JsonSerializerRegistry {

  override def serializers: immutable.Seq[JsonSerializer[_]] = immutable.Seq(
    JsonSerializer(format[SequenceState]),
    JsonSerializer(emptySingletonFormat(GetNextId)),
    JsonSerializer(emptySingletonFormat(NextIdGenerated)))
}
