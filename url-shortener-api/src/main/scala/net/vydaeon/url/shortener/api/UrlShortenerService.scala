package net.vydaeon.url.shortener.api

import com.lightbend.lagom.scaladsl.api.Service
import com.lightbend.lagom.scaladsl.api.Service.named
import com.lightbend.lagom.scaladsl.api.Service.pathCall
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.HeaderFilter
import com.lightbend.lagom.scaladsl.api.transport.RequestHeader
import com.lightbend.lagom.scaladsl.api.transport.ResponseHeader

import akka.NotUsed

trait UrlShortenerService extends Service {

  /**
   * @return a [[ServiceCall]] taking a plain-text request body with a full URL,
   * and returning a plain-text response with a short URL path (for the current host).
   */
  def createShortUrlPath: ServiceCall[String, String]

  /**
   * @param shortUrlFragment The path fragment from a short URL.
   * @return a [[ServiceCall]] taking no request body and returning no response body;
   * the service call redirects to the full URL for the short URL.
   */
  def redirectToLongUrl(shortUrlFragment: String): ServiceCall[NotUsed, NotUsed]

  override def descriptor = named("urlShortener")
    .withCalls(
      pathCall("/shortUrl", createShortUrlPath),
      pathCall("/:fragment", redirectToLongUrl _))
    .withHeaderFilter(AccessControlAllowOriginFilter)
    .withAutoAcl(true)
}

object AccessControlAllowOriginFilter extends HeaderFilter {

  override def transformServerResponse(response: ResponseHeader, request: RequestHeader) =
    response.addHeader("Access-Control-Allow-Origin", "*")

  override def transformServerRequest(request: RequestHeader) = request
  override def transformClientRequest(request: RequestHeader) = request
  override def transformClientResponse(response: ResponseHeader, request: RequestHeader) = response
}
