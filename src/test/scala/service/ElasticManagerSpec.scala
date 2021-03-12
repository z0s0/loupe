package service

import loupe.ElasticConfig
import loupe.service.ElasticManager
import loupe.{ElasticClient => Client}
import org.testcontainers.elasticsearch.ElasticsearchContainer
import zio.{Has, URLayer, ZLayer}
import zio.test.DefaultRunnableSpec
import zio.test._
import zio.test.Assertion._
import zio.blocking.{Blocking, effectBlocking}
import zio.test.TestAspect._

object ElasticManagerSpec extends DefaultRunnableSpec {
  val elasticsearchImage =
    "docker.elastic.co/elasticsearch/elasticsearch:7.11.2"

  val containerLayer: ZLayer[Blocking, Throwable, Has[ElasticsearchContainer]] =
    ZLayer.fromAcquireRelease(effectBlocking {
      val container =
        new ElasticsearchContainer(elasticsearchImage).withReuse(true)
      container.start()
      container
    })(cont => effectBlocking(cont.stop()).orDie)

  val elasticConfig: URLayer[Has[ElasticsearchContainer], Has[ElasticConfig]] =
    ZLayer.fromService(
      cont => ElasticConfig(uris = List("http://" + cont.getHttpHostAddress))
    )

  val layer = containerLayer >>> elasticConfig >>> Client.live >>> ElasticManager.live

  override def spec = {
    suite("ElasticManager")(
      suite("hasIndex")(testM("returns if index exists") {
        for {
          _ <- ElasticManager.createSchema("existing")
          notExistingExists <- ElasticManager.hasIndex("not_existing_index")
          existingExists <- ElasticManager.hasIndex("existing")
        } yield {
          assert(notExistingExists)(isFalse) &&
          assert(existingExists)(isTrue)
        }
      }),
      suite("createSchema")(
        testM("adds new schema if index does not exist") {
          for {
            resp <- ElasticManager.createSchema("new_index")
          } yield assert(resp)(isTrue)
        },
        testM("createSchema returns Conflict() if index exists") {
          val effect = ElasticManager.createSchema("idx")

          for {
            _ <- effect
            eff <- effect.run
          } yield assert(eff)(fails(equalTo(Conflict("schema already exists"))))
        }
      )
    ).@@(after(ElasticManager.cleanAll.orDie))
      .provideCustomLayer(layer.orDie)
  }

}
