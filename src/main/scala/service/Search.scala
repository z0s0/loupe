package service

import com.sksamuel.elastic4s.{ElasticClient, Response}
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import com.sksamuel.elastic4s.zio.instances._
import zio.{Has, Task, URLayer, ZLayer}

object Search {
  type Search = Has[Service]
  type SearchResult = Map[String, String]

  trait Service {
    def find(schemaName: String, input: String): Task[Response[SearchResponse]]
  }

  val live: URLayer[Has[ElasticClient], Search] = ZLayer.fromService { client =>
    new Service {
      override def find(schemaName: String,
                        input: String): Task[Response[SearchResponse]] =
        client
          .execute {
            search(schemaName).query(input)
          }
          .map(resp => resp)
    }
  }
}
