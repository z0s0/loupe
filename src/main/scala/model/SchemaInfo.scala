package model
import io.circe._
import io.circe.generic.semiauto.{deriveEncoder, deriveDecoder}

object SchemaInfo {
  implicit val encoder: Encoder[SchemaInfo] = deriveEncoder
  implicit val decoder: Decoder[SchemaInfo] = deriveDecoder
}

final case class SchemaInfo(name: String,
                            documentsCount: Int,
                            schema: Map[String, String])
