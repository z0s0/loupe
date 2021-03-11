package service

import com.sksamuel.elastic4s.ElasticClient
import loupe.ElasticConfig
import loupe.service.ElasticManager
import loupe.{ElasticClient => Client}
import org.testcontainers.elasticsearch.ElasticsearchContainer
import zio.{Has, UIO, URLayer, ZIO, ZLayer}
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
      val container = new ElasticsearchContainer(elasticsearchImage)
      container.start()
      container
    })(cont => effectBlocking(cont.stop()).orDie)

  val elasticConfig: URLayer[Has[ElasticsearchContainer], Has[ElasticConfig]] =
    ZLayer.fromService(
      cont => ElasticConfig(uris = List("http://" + cont.getHttpHostAddress))
    )

  val layer = containerLayer >>> elasticConfig >>> Client.live >>> ElasticManager.live

  override def spec =
    suite("ElasticManager")(
      testM("hasIndex return false unless index exists. Returns true if exists") {
        for {
          _ <- ElasticManager.createSchema("existing")
          notExistingExists <- ElasticManager.hasIndex("not_existing_index")
          existingExists <- ElasticManager.hasIndex("existing")
        } yield {
          assert(notExistingExists)(isFalse) &&
          assert(existingExists)(isTrue)
        }
      }
    ).@@(after(ElasticManager.cleanAll.orDie))
      .provideSomeLayerShared[Environment](layer.orDie)

}
