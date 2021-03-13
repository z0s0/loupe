package loupe.api

import java.io.IOException

import loupe.model.UploadData
import loupe.model.errors.{Conflict, Disaster, Invalid, SomethingWentWrong}
import loupe.service.ElasticManager
import sttp.tapir.ztapir._
import zio.{IO, Managed, ZIO, ZManaged}

import scala.io.Source

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
        hasIndex(name)
          .flatMap(
            schemaExists =>
              ElasticManager
                .removeIndex(name)
                .catchAll(_ => IO.fail(SomethingWentWrong))
                .when(schemaExists)
          )
      }

  val loadDataLogic = Docs.loadData.zServerLogic {
    case (schema, payload) =>
      Source
        .fromFile(payload.data.body)
        .getLines()
        .foreach(println)

      hasIndex(schema).flatMap(indexExists => IO.unit)
  }

  private def openFile(file: TapirFile) = {
    val acquire = ZIO(Source.fromFile(file))
    val release = (source: Source) => ZIO(source.close()).orDie

    ZManaged.make(acquire)(release)
  }
  private def hasIndex(name: String) =
    ElasticManager
      .hasIndex(name)
      .catchAll(_ => IO.fail(SomethingWentWrong))

  val routes =
    List(listSchemasLogic, createSchemaLogic, deleteSchemaLogic, loadDataLogic)
}
