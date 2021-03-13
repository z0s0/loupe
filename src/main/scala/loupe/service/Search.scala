package loupe.service

import com.sksamuel.elastic4s.{ElasticClient, Response}
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import com.sksamuel.elastic4s.zio.instances._
import loupe.model.errors.Disaster
import zio.{Has, IO, URLayer, ZIO, ZLayer}

object Search {
  type Search = Has[Service]
  type SearchResult = Response[SearchResponse]

  trait Service {
    def find(schemaName: String, input: String): IO[Disaster, SearchResult]
  }

  val live: URLayer[Has[ElasticClient], Search] = ZLayer.fromService {
    client => (schemaName: String, input: String) =>
      client
        .execute {
          search(schemaName).query(input)
        }
        .map(resp => resp)
        .catchAll(_ => IO.fail(Disaster("something went wrong")))
  }

  def find(schemaName: String,
           input: String): ZIO[Search, Disaster, SearchResult] =
    ZIO.accessM(_.get.find(schemaName, input))
}
