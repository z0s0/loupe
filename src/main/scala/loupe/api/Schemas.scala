package loupe.api

import loupe.model.SchemaInfo
import loupe.service.ElasticManager
import loupe.service.ElasticManager.ElasticManager
import sttp.tapir.ztapir._
import zio.IO

object Schemas {
  val schemasLogic
    : ZServerEndpoint[ElasticManager, Unit, String, List[SchemaInfo]] =
    Docs.schemas.zServerLogic { _ =>
      ElasticManager.listSchemas.catchAll(_ => IO.fail("network err"))
    }

  val routes = List(schemasLogic)
}
