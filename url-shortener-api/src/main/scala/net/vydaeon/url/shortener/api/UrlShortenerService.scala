package net.vydaeon.url.shortener.api

import com.lightbend.lagom.scaladsl.api.Service
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.HeaderFilter
import com.lightbend.lagom.scaladsl.api.transport.RequestHeader
import com.lightbend.lagom.scaladsl.api.transport.ResponseHeader

import akka.NotUsed

trait UrlShortenerService extends Service {

  val pathPrefix = "/url"

  def createShortUrlPath: ServiceCall[String, String]

  def redirectToLongUrl(shortUrlFragment: String, index: Int): ServiceCall[NotUsed, NotUsed]

  override def descriptor = {
    import com.lightbend.lagom.scaladsl.api.Service._
    named("urlShortener")
      .withCalls(
        pathCall(pathPrefix, createShortUrlPath),
        pathCall(pathPrefix + "/:fragment/:index", redirectToLongUrl _))
      .withHeaderFilter(AccessControlAllowOriginFilter)
      .withAutoAcl(true)
  }
}

object AccessControlAllowOriginFilter extends HeaderFilter {

  override def transformServerResponse(response: ResponseHeader, request: RequestHeader) =
    response.addHeader("Access-Control-Allow-Origin", "*")

  override def transformClientRequest(request: RequestHeader) = request
  override def transformServerRequest(request: RequestHeader) = request
  override def transformClientResponse(response: ResponseHeader, request: RequestHeader) = response
}
