package loupe.api

import loupe.model.errors.{Conflict, Disaster, Invalid, SomethingWentWrong}
import loupe.service.ElasticManager
import sttp.tapir.ztapir._
import zio.IO

object Schemas {
  val listSchemasLogic =
    Docs.listSchemas.zServerLogic { _ =>
      ElasticManager.listSchemas.catchAll(_ => IO.fail(SomethingWentWrong))
    }

  val createSchemaLogic =
    Docs.createSchema
      .zServerLogic(
        params =>
          ElasticManager.createSchema(params).refineOrDie {
            case Conflict(reason) => Invalid(List(reason))
            case Disaster(_)      => SomethingWentWrong
        }
      )

  val deleteSchemaLogic =
    Docs.deleteSchema
      .zServerLogic { name =>
        ElasticManager
          .hasIndex(name)
          .catchAll(_ => IO.fail(SomethingWentWrong))
          .flatMap(
            schemaExists =>
              ElasticManager
                .removeIndex(name)
                .catchAll(_ => IO.fail(SomethingWentWrong))
                .when(schemaExists)
          )
      }

  val routes = List(listSchemasLogic, createSchemaLogic, deleteSchemaLogic)
}
