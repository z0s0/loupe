package api
import org.http4s.HttpRoutes
import service.ElasticInfo.ElasticInfo
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.ztapir._
import zio.IO
import service.{ElasticInfo, Search => SearchService}
import zio.interop.catz._

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

  def routes =
    ZHttp4sServerInterpreter.from(searchLogic).toRoutes
}
