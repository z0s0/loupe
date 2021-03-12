package service

import loupe.ElasticConfig
import loupe.model.SchemaInfo
import loupe.model.errors.Conflict
import loupe.model.params.CreateSchemaParams
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

  def createSchemaParams(name: String) =
    CreateSchemaParams(name = name, lang = "en", mappings = Map("a" -> "int"))

  override def spec = {
    suite("ElasticManager")(
      suite("hasIndex")(testM("returns if index exists") {
        for {
          _ <- ElasticManager.createSchema(createSchemaParams("existing"))
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
            resp <- ElasticManager.createSchema(createSchemaParams("new_index"))
          } yield
            assert(resp)(
              equalTo(SchemaInfo(name = "new_index", documentsCount = 0))
            )
        },
        testM("createSchema returns Conflict() if index exists") {
          val effect = ElasticManager.createSchema(createSchemaParams("idx"))

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
