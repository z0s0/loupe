package loupe.service

import com.sksamuel.elastic4s.{ElasticClient, RequestFailure, RequestSuccess}
import zio.{Has, IO, Task, URLayer, ZIO, ZLayer}
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.zio.instances._
import loupe.model.params.CreateSchemaParams
import loupe.model.SchemaInfo
import loupe.model.errors.{Conflict, Disaster, ElasticError}

object ElasticManager {
  type ElasticManager = Has[Service]

  trait Service {
    def listSchemas: Task[List[SchemaInfo]]
    def hasIndex(indexName: String): Task[Boolean]
    def createSchema(params: CreateSchemaParams): IO[ElasticError, SchemaInfo]

    private[service] def cleanAll: Task[Boolean]
  }

  val live: URLayer[Has[ElasticClient], ElasticManager] = ZLayer.fromService {
    client =>
      new Service {
        override def listSchemas: Task[List[SchemaInfo]] =
          for {
            indexesResponses <- client.execute(catIndices).map(_.result.toList)
          } yield
            indexesResponses.map(
              resp => SchemaInfo(name = resp.index, documentsCount = resp.count)
            )

        override def hasIndex(indexName: String): Task[Boolean] = {
          client
            .execute(indexExists(indexName))
            .map(_.result.exists)
        }

        def createSchema(
          params: CreateSchemaParams
        ): IO[ElasticError, SchemaInfo] =
          client
            .execute(createIndex(params.name))
            .catchAll(_ => IO.fail(Disaster("Network error")))
            .flatMap {
              case RequestSuccess(_, _, _, _) =>
                IO.succeed(SchemaInfo(name = params.name, documentsCount = 0))
              case RequestFailure(_, _, _, _) =>
                IO.fail(Conflict("schema already exists"))
            }

        override private[service] def cleanAll: Task[Boolean] =
          client
            .execute(deleteIndex("*"))
            .map(_.result.acknowledged)

      }
  }

  def listSchemas: ZIO[ElasticManager, Throwable, List[SchemaInfo]] =
    ZIO.accessM(_.get.listSchemas)
  def hasIndex(indexName: String): ZIO[ElasticManager, Throwable, Boolean] =
    ZIO.accessM(_.get.hasIndex(indexName))
  def cleanAll: ZIO[ElasticManager, Throwable, Boolean] =
    ZIO.accessM(_.get.cleanAll)
  def createSchema(
    params: CreateSchemaParams
  ): ZIO[ElasticManager, ElasticError, SchemaInfo] =
    ZIO.accessM(_.get.createSchema(params))
}
