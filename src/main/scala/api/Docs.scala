package api

import model.SchemaInfo
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.generic.auto._
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.ztapir._

object Docs {
  val schemas =
    endpoint.get
      .in("schemas")
      .out(jsonBody[Vector[SchemaInfo]])

}
