package net.vydaeon.url.shortener.impl

import scala.concurrent.Future

import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.AsyncWordSpec

import com.lightbend.lagom.scaladsl.api.transport.BadRequest
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.UnhandledCommandException

import akka.Done
import akka.NotUsed

class UrlShortenerServiceImplSpec extends AsyncWordSpec with AsyncMockFactory {

  val entityService = mock[EntityService]
  val urlShortenerService = new UrlShortenerServiceImpl(entityService)

  "UrlShortenerServiceImpl" should {

    "create short URL" in {
      val id = 1123
      val shortUrlFragment = "BGM"
      val longUrl = "http://www.google.com/"
      (entityService.getNextId _) expects sequenceIdWithinShardCount returns Future.successful(id)
      (entityService.setFullUrl _) expects (shortUrlFragment, longUrl) returns Future.successful(Done)
      urlShortenerService.createShortUrlPath.invoke(longUrl).map { response =>
        assert(response == ("/" + shortUrlFragment))
      }
    }

    "reject invalid URL" in {
      val invalidUrl = "foobar"
      val badRequest = intercept[BadRequest] {
        urlShortenerService.createShortUrlPath.invoke(invalidUrl)
      }
      assert(badRequest.exceptionMessage.detail contains invalidUrl)
    }

    "redirect to long URL" in {
      val shortUrlFragment = "shortUrlFragment"
      val fullUrl = "http://www.google.com/"
      (entityService.getFullUrl _) expects shortUrlFragment returns Future.successful(fullUrl)
      urlShortenerService.redirectToLongUrl(shortUrlFragment).withResponseHeader.invoke(NotUsed).map {
        case (responseHeader, _) => {
          assert(responseHeader.status == 301)
          assert(responseHeader.getHeader("location") == Some(fullUrl))
        }
      }
    }

    "reject invalid fragment" in {
      val invalidFragment = "invalidFragment"
      (entityService.getFullUrl _) expects invalidFragment returns Future.failed(UnhandledCommandException("TEST TEST TEST"))
      urlShortenerService.redirectToLongUrl(invalidFragment).withResponseHeader
        .invoke(NotUsed).failed.map(_ match {
          case badRequest: BadRequest => assert(badRequest.exceptionMessage.detail contains invalidFragment)
          case other                  => fail("unexpected failure: " + other)
        })
    }
  }

  private def sequenceIdWithinShardCount = where { (sequenceId: String) =>
    {
      val index = sequenceId.toInt
      index >= 0 && index < ShortenedUrlIdSequence.SHARD_COUNT
    }
  }
}
