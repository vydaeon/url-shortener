package net.vydaeon.url.shortener.impl

import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Base64

import scala.concurrent.ExecutionContext

import com.google.common.hash.Hashing
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.ResponseHeader
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.server.ServerServiceCall

import akka.NotUsed
import net.vydaeon.url.shortener.api.UrlShortenerService

class UrlShortenerServiceImpl(persistentEntityRegistry: PersistentEntityRegistry)(implicit ec: ExecutionContext)
  extends UrlShortenerService {

  override def createShortUrlPath = ServiceCall { urlString =>
    val normalizedUrlString = new URL(urlString).toURI.normalize.toString
    val urlBytes = normalizedUrlString.getBytes(StandardCharsets.UTF_8)
    val hashBytes = Hashing.murmur3_128.hashBytes(urlBytes).asBytes
    val shortUrlFragment = Base64.getUrlEncoder.encodeToString(hashBytes)
    persistentEntityRegistry.refFor[ShortenedUrl](shortUrlFragment)
      .ask(SetFullUrl(normalizedUrlString))
      .map(index => pathPrefix + "/" + shortUrlFragment + "/" + index)
  }

  override def redirectToLongUrl(shortUrlFragment: String, index: Int) = ServerServiceCall { (_, _) =>
    persistentEntityRegistry.refFor[ShortenedUrl](shortUrlFragment)
      .ask(GetFullUrl(index))
      .map { fullUrl =>
        val responseHeader = ResponseHeader.Ok
          .withStatus(301)
          .withHeader("location", fullUrl)
        (responseHeader, NotUsed)
      }
  }
}
