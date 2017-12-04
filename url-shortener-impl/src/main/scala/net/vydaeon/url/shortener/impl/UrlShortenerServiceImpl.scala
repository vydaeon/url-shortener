package net.vydaeon.url.shortener.impl

import java.math.BigInteger
import java.net.URL
import java.util.Base64

import scala.concurrent.ExecutionContext
import scala.util.Random

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import com.lightbend.lagom.scaladsl.api.transport.ResponseHeader
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.UnhandledCommandException
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.server.ServerServiceCall

import akka.NotUsed
import net.vydaeon.url.shortener.api.UrlShortenerService

class UrlShortenerServiceImpl(entityService: EntityService)(implicit ec: ExecutionContext)
  extends UrlShortenerService {

  override def createShortUrlPath = ServiceCall { urlString =>
    val fullUrl = validateUrl(urlString)
    val sequenceId = pickSequenceId
    entityService.getNextId(sequenceId)
      .flatMap(toShortUrl(fullUrl))
  }

  private def validateUrl(urlString: String) =
    try {
      new URL(urlString).toURI.toString
    } catch {
      case e: Exception => throw BadRequest("invalid URL " + urlString)
    }

  private def pickSequenceId = Random.nextInt(ShortenedUrlIdSequence.SHARD_COUNT).toString

  private def toShortUrl(normalizedUrlString: String)(id: Long) = {
    val idBytes = BigInteger.valueOf(id).toByteArray
    val shortUrlFragment = Base64.getUrlEncoder.withoutPadding.encodeToString(idBytes)
    entityService.setFullUrl(shortUrlFragment, normalizedUrlString)
      .map { _ => "/" + shortUrlFragment }
  }

  override def redirectToLongUrl(shortUrlFragment: String) = ServerServiceCall { (_, _) =>
    entityService.getFullUrl(shortUrlFragment)
      .map(toRedirect)
      .recover({
        case UnhandledCommandException(_) => throw BadRequest("invalid short URL /" + shortUrlFragment)
      })
  }

  private def toRedirect(fullUrl: String) = {
    val responseHeader = ResponseHeader.Ok
      .withStatus(301)
      .withHeader("location", fullUrl)
    (responseHeader, NotUsed)
  }
}

/**
 * Encapsulates the lookup of and initial command to persistent entities;
 * used to mock the persistence layer in unit tests.
 */
class EntityService(entityRegistry: PersistentEntityRegistry) {

  def getNextId(sequenceId: String) = entityRegistry.refFor[ShortenedUrlIdSequence](sequenceId)
    .ask(GetNextId)

  def setFullUrl(shortUrlFragment: String, normalizedUrlString: String) =
    entityRegistry.refFor[ShortenedUrl](shortUrlFragment)
      .ask(SetFullUrl(normalizedUrlString))

  def getFullUrl(shortUrlFragment: String) = entityRegistry.refFor[ShortenedUrl](shortUrlFragment)
    .ask(GetFullUrl)
}
