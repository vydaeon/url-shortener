package net.vydaeon.url.shortener.impl

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpec

import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver

import ShortenedUrlIdSequence.SHARD_COUNT
import akka.actor.ActorSystem

class ShortenedUrlIdSequenceSpec extends WordSpec with BeforeAndAfterAll {

  val system = ActorSystem(
    "ShortenedUrlIdSequenceSpec",
    JsonSerializerRegistry.actorSystemSetupFor(SequenceSerializerRegistry))

  override def afterAll = Await.ready(system.terminate, 10.seconds)

  "ShortenedUrlIdSequence" should {

    "generate IDs from 0" in {
      val driver = new PersistentEntityTestDriver(system, new ShortenedUrlIdSequence, "0")
      val outcome = driver.run(GetNextId, GetNextId)
      assert(outcome.events == List(NextIdGenerated, NextIdGenerated))
      assert(outcome.state.nextId == SHARD_COUNT * 2)
      assert(outcome.replies == List(0, SHARD_COUNT))
      assert(outcome.issues == Nil)
    }

    "generate IDs from 1" in {
      val driver = new PersistentEntityTestDriver(system, new ShortenedUrlIdSequence, "1")
      val outcome = driver.run(GetNextId, GetNextId)
      assert(outcome.events == List(NextIdGenerated, NextIdGenerated))
      assert(outcome.state.nextId == SHARD_COUNT * 2 + 1)
      assert(outcome.replies == List(1, SHARD_COUNT + 1))
      assert(outcome.issues == Nil)
    }
  }
}
