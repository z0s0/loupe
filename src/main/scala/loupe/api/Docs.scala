package loupe.api

import loupe.model.SchemaInfo
import loupe.model.errors.ClientError
import loupe.model.params.CreateSchemaParams
import sttp.model.StatusCode
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.generic.auto._
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.ztapir._

object Docs {
  val listSchemas =
    endpoint.get
      .in("schemas")
      .out(jsonBody[List[SchemaInfo]])
      .errorOut(jsonBody[ClientError])

  val createSchema =
    endpoint.post
      .in("schemas")
      .in(jsonBody[CreateSchemaParams])
      .out(jsonBody[SchemaInfo])
      .out(statusCode(StatusCode.Created))
      .errorOut(jsonBody[ClientError])
      .errorOut(statusCode(StatusCode.BadRequest))

  val deleteSchema =
    endpoint.delete
      .in("schemas" / path[String])
      .out(statusCode(StatusCode.Found))
      .errorOut(statusCode(StatusCode.NotFound))
      .errorOut(jsonBody[ClientError])

  val search =
    endpoint.get
      .in("search" / path[String])
      .in(query[String]("q"))
      .out(jsonBody[Map[String, String]])
      .errorOut(statusCode(StatusCode.NotFound))
      .errorOut(jsonBody[ClientError])

  val docs = List(listSchemas, search, deleteSchema, createSchema)

  val yaml = OpenAPIDocsInterpreter.toOpenAPI(docs, "Loupe", "1").toYaml
}
