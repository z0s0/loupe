package loupe.api

import loupe.service.ElasticManager.ElasticManager
import loupe.service.ElasticManager
import loupe.service.Search.Search
import loupe.service.{Search => SearchService}
import sttp.tapir.ztapir._
import zio.IO

object Search {
  type Deps = ElasticManager with Search

  val searchLogic
    : ZServerEndpoint[Deps, (String, String), String, Map[String, String]] =
    Docs.search.zServerLogic {
      case (schema, query) =>
        ElasticManager
          .hasIndex(schema)
          .catchAll(_ => IO.fail("elastic error"))
          .flatMap {
            case true =>
              SearchService
                .find(schema, query)
                .map(res => {
                  println(res)
                  Map[String, String]("foo" -> "bar")
                })

            case false =>
              IO.fail("schema does not exist")
          }
    }

  def routes = List(searchLogic)
}
