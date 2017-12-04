package net.vydaeon.url.shortener.impl

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpec

import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver.UnhandledCommand

import akka.Done
import akka.actor.ActorSystem

class ShortenedUrlSpec extends WordSpec with BeforeAndAfterAll {

  val system = ActorSystem(
    "ShortenedUrlSpec",
    JsonSerializerRegistry.actorSystemSetupFor(ShortenedUrlSerializerRegistry))

  override def afterAll = Await.ready(system.terminate, 10.seconds)

  "ShortenedUrl" should {

    "accept full URL" in {
      val driver = new PersistentEntityTestDriver(system, new ShortenedUrl, "fragment")
      val fullUrl = "fullUrl"
      val outcome = driver.run(SetFullUrl(fullUrl))
      assert(outcome.events == List(FullUrlSet(fullUrl)))
      assert(outcome.state.fullUrlOption == Some(fullUrl))
      assert(outcome.replies == List(Done))
      assert(outcome.issues == Nil)
    }

    "reject 2nd full URL" in {
      val driver = new PersistentEntityTestDriver(system, new ShortenedUrl, "fragment")
      val fullUrl = "fullUrl"
      driver.run(SetFullUrl(fullUrl))

      val command = SetFullUrl("anotherUrl")
      val outcome = driver.run(command)
      assert(outcome.events == Nil)
      assert(outcome.state.fullUrlOption == Some(fullUrl))
      assert(outcome.replies == Nil)
      assert(outcome.issues == List(UnhandledCommand(command)))
    }

    "provide full URL" in {
      val driver = new PersistentEntityTestDriver(system, new ShortenedUrl, "fragment")
      val fullUrl = "fullUrl"
      driver.run(SetFullUrl(fullUrl))

      val outcome = driver.run(GetFullUrl)
      assert(outcome.events == Nil)
      assert(outcome.replies == List(fullUrl))
      assert(outcome.issues == Nil)
    }
  }
}
