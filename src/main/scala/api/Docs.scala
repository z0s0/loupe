package api

import model.SchemaInfo
import sttp.model.StatusCode
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.generic.auto._
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.ztapir._

object Docs {
  val schemas =
    endpoint.get
      .in("schemas")
      .out(jsonBody[Vector[SchemaInfo]])
      .errorOut(stringBody)

  val search =
    endpoint.get
      .in("search" / path[String])
      .in(query[String]("q"))
      .out(jsonBody[Map[String, String]])
      .errorOut(statusCode(StatusCode.NotFound))
      .errorOut(stringBody)

  val docs = List(schemas, search)

  val yaml = OpenAPIDocsInterpreter.toOpenAPI(docs, "Loupe", "1").toYaml
}
