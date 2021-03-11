package service

import com.sksamuel.elastic4s.ElasticClient
import model.SchemaInfo
import zio.{Has, IO, Task, UIO, URLayer, ZIO, ZLayer}
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.zio.instances._

object ElasticInfo {
  type ElasticInfo = Has[Service]

  trait Service {
    def listSchemas: Task[List[SchemaInfo]]
    def hasIndex(indexName: String): Task[Boolean]
  }

  val live: URLayer[Has[ElasticClient], ElasticInfo] = ZLayer.fromService {
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
      }
  }

  def listSchemas: ZIO[ElasticInfo, Throwable, List[SchemaInfo]] =
    ZIO.accessM(_.get.listSchemas)
  def hasIndex(indexName: String): ZIO[ElasticInfo, Throwable, Boolean] =
    ZIO.accessM(_.get.hasIndex(indexName))
}
