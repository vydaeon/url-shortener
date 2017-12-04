package net.vydaeon.url.shortener.impl

import scala.collection.immutable

import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer.emptySingletonFormat
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry

import akka.Done
import play.api.libs.json.Json.format

/**
 * Stores the full URL for a short URL fragment (the entity ID).
 */
class ShortenedUrl extends PersistentEntity {

  override type State = ShortenedUrlState
  override type Command = ShortenedUrlCommand
  override type Event = ShortenedUrlEvent

  override def initialState = ShortenedUrlState(None)

  override def behavior: Behavior = {
    case ShortenedUrlState(None)          => acceptFullUrl
    case ShortenedUrlState(Some(fullUrl)) => provideFullUrl(fullUrl)
  }

  private def acceptFullUrl = Actions()
    .onCommand[SetFullUrl, Done] {
      case (SetFullUrl(fullUrl), ctx, state) => setFullUrl(fullUrl, ctx, state)
    }
    .onEvent {
      case (FullUrlSet(fullUrl), state) => fullUrlSet(fullUrl, state)
    }

  private def setFullUrl(fullUrl: String, ctx: CommandContext[Done], state: State) =
    ctx.thenPersist(FullUrlSet(fullUrl)) { _ =>
      ctx.reply(Done)
    }

  private def fullUrlSet(fullUrl: String, state: State) = ShortenedUrlState(Some(fullUrl))

  private def provideFullUrl(fullUrl: String) = Actions()
    .onReadOnlyCommand[GetFullUrl.type, String] {
      case (_: GetFullUrl.type, ctx, _) => getFullUrl(ctx, fullUrl)
    }

  private def getFullUrl(ctx: ReadOnlyCommandContext[String], fullUrl: String) = ctx.reply(fullUrl)
}

final case class ShortenedUrlState(fullUrlOption: Option[String])

sealed trait ShortenedUrlCommand

final case class SetFullUrl(fullUrl: String) extends ShortenedUrlCommand with ReplyType[Done]

final case object GetFullUrl extends ShortenedUrlCommand with ReplyType[String]

sealed trait ShortenedUrlEvent

final case class FullUrlSet(fullUrl: String) extends ShortenedUrlEvent

object ShortenedUrlSerializerRegistry extends JsonSerializerRegistry {

  override def serializers: immutable.Seq[JsonSerializer[_]] = immutable.Seq(
    JsonSerializer(format[ShortenedUrlState]),
    JsonSerializer(format[SetFullUrl]),
    JsonSerializer(emptySingletonFormat(GetFullUrl)),
    JsonSerializer(format[FullUrlSet]))
}
