package loupe.api

import loupe.model.errors.{ClientError, NotFound, SomethingWentWrong}
import loupe.service.ElasticManager.ElasticManager
import loupe.service.ElasticManager
import loupe.service.Search.Search
import loupe.service.{Search => SearchService}
import sttp.tapir.ztapir._
import zio.IO

object Search {
  type Deps = ElasticManager with Search

  val searchLogic =
    Docs.search.zServerLogic {
      case (schema, query) =>
        ElasticManager
          .hasIndex(schema)
          .catchAll(_ => IO.fail(SomethingWentWrong))
          .flatMap {
            case true =>
              SearchService
                .find(schema, query)
                .refineOrDie(_ => SomethingWentWrong)
                .as({
                  Map[String, String]("foo" -> "bar")
                })

            case false => IO.fail(NotFound)
          }
    }

  def routes = List(searchLogic)
}
