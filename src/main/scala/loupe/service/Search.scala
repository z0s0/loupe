package loupe.service

import com.sksamuel.elastic4s.{ElasticClient, Response}
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import com.sksamuel.elastic4s.zio.instances._
import zio.{Has, IO, URLayer, ZIO, ZLayer}

object Search {
  type Search = Has[Service]
  type SearchResult = Response[SearchResponse]

  trait Service {
    def find(schemaName: String, input: String): IO[String, SearchResult]
  }

  val live: URLayer[Has[ElasticClient], Search] = ZLayer.fromService {
    client => (schemaName: String, input: String) =>
      client
        .execute {
          search(schemaName).query(input)
        }
        .map(resp => resp)
        .catchAll(_ => IO.fail("something went wrong"))
  }

  def find(schemaName: String,
           input: String): ZIO[Search, String, SearchResult] =
    ZIO.accessM(_.get.find(schemaName, input))
}
