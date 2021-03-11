package api
import model.SchemaInfo
import service.ElasticInfo
import service.ElasticInfo.ElasticInfo
import sttp.tapir.ztapir._
import zio.IO

object Schemas {
  val schemasLogic
    : ZServerEndpoint[ElasticInfo, Unit, String, List[SchemaInfo]] =
    Docs.schemas.zServerLogic { _ =>
      ElasticInfo.listSchemas.catchAll(_ => IO.fail("network err"))
    }

  val routes = List(schemasLogic)
}
