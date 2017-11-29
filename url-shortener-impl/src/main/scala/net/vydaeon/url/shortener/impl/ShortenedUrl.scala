package net.vydaeon.url.shortener.impl

import scala.collection.immutable

import com.lightbend.lagom.scaladsl.persistence.AggregateEvent
import com.lightbend.lagom.scaladsl.persistence.AggregateEventTag
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry

import play.api.libs.json.Json

class ShortenedUrl extends PersistentEntity {

  override type State = ShortenedUrlState
  override type Command = ShortenedUrlCommand
  override type Event = ShortenedUrlEvent

  override def initialState = ShortenedUrlState.initial

  override def behavior = Actions()
    .onCommand[SetFullUrl, Int] {
      case (SetFullUrl(fullUrl), ctx, state) => setFullUrl(fullUrl, ctx, state)
    }
    .onEvent {
      case (FullUrlSet(fullUrl), state) => setFullUrl(fullUrl, state)
    }
    .onReadOnlyCommand[GetFullUrl, String] {
      case (GetFullUrl(index), ctx, state) => getFullUrl(index, ctx, state)
    }

  private def setFullUrl(fullUrl: String, ctx: CommandContext[Int], state: ShortenedUrlState) = {
    val existingIndex = state.fullUrls.indexOf(fullUrl)
    if (existingIndex >= 0) {
      ctx.reply(existingIndex)
      ctx.done
    } else {
      val nextIndex = state.fullUrls.size
      ctx.thenPersist(FullUrlSet(fullUrl)) { _ =>
        ctx.reply(nextIndex)
      }
    }
  }

  private def setFullUrl(fullUrl: String, state: ShortenedUrlState) =
    ShortenedUrlState(state.fullUrls :+ fullUrl)

  private def getFullUrl(index: Int, ctx: ReadOnlyCommandContext[String], state: ShortenedUrlState) = {
    val fullUrl = state.fullUrls(index)
    ctx.reply(fullUrl)
  }
}

final case class ShortenedUrlState(fullUrls: List[String])

object ShortenedUrlState {

  val initial = ShortenedUrlState(Nil)
  implicit val format = Json.format[ShortenedUrlState]
}

sealed trait ShortenedUrlCommand

final case class SetFullUrl(fullUrl: String)
  extends ShortenedUrlCommand with ReplyType[Int]

object SetFullUrl {

  implicit val format = Json.format[SetFullUrl]
}

final case class GetFullUrl(index: Int)
  extends ShortenedUrlCommand with ReplyType[String]

object GetFullUrl {

  implicit val format = Json.format[GetFullUrl]
}

object ShortenedUrlEvent {

  val tag = AggregateEventTag[ShortenedUrlEvent]
}

sealed trait ShortenedUrlEvent extends AggregateEvent[ShortenedUrlEvent] {

  override def aggregateTag = ShortenedUrlEvent.tag
}

final case class FullUrlSet(fullUrl: String) extends ShortenedUrlEvent

object FullUrlSet {

  implicit val format = Json.format[FullUrlSet]
}

object ShortenedUrlSerializerRegistry extends JsonSerializerRegistry {

  override def serializers: immutable.Seq[JsonSerializer[_]] = immutable.Seq(
    JsonSerializer[ShortenedUrlState],
    JsonSerializer[SetFullUrl],
    JsonSerializer[GetFullUrl],
    JsonSerializer[FullUrlSet])
}
