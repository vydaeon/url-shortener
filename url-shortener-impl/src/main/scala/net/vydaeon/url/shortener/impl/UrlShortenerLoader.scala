package net.vydaeon.url.shortener.impl

import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.dns.DnsServiceLocatorComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server.LagomApplication
import com.lightbend.lagom.scaladsl.server.LagomApplicationContext
import com.lightbend.lagom.scaladsl.server.LagomApplicationLoader
import com.softwaremill.macwire.wire

import net.vydaeon.url.shortener.api.UrlShortenerService
import play.api.libs.ws.ahc.AhcWSComponents

class UrlShortenerLoader extends LagomApplicationLoader {

  override def loadDevMode(context: LagomApplicationContext) =
    new UrlShortenerApplication(context) with LagomDevModeComponents

  override def load(context: LagomApplicationContext) =
    new UrlShortenerApplication(context) with DnsServiceLocatorComponents

  override def describeService = Some(readDescriptor[UrlShortenerService])
}

abstract class UrlShortenerApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
  with AhcWSComponents
  with CassandraPersistenceComponents {

  lazy val entityService = wire[EntityService]

  override lazy val lagomServer = serverFor[UrlShortenerService](wire[UrlShortenerServiceImpl])
  override lazy val jsonSerializerRegistry =
    SequenceSerializerRegistry ++
      ShortenedUrlSerializerRegistry

  persistentEntityRegistry.register(wire[ShortenedUrl])
  persistentEntityRegistry.register(wire[ShortenedUrlIdSequence])
}
