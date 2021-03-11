package api
import service.ElasticInfo.ElasticInfo
import sttp.tapir.ztapir._
import zio.IO
import service.{ElasticInfo, Search => SearchService}

object Search {
  type Deps = ElasticInfo with SearchService.Search

  val searchLogic
    : ZServerEndpoint[Deps, (String, String), String, Map[String, String]] =
    Docs.search.zServerLogic {
      case (schema, query) =>
        ElasticInfo
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
