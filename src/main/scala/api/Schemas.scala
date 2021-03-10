package api
import model.SchemaInfo
import sttp.tapir.ztapir._
import zio.UIO

object Schemas {
  val schemasLogic = Docs.schemas.zServerLogic { _ =>
    UIO(Vector(SchemaInfo("cities", 10000, Map[String, String]())))

  }
}
