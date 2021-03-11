package loupe.model

import io.circe._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

object SchemaInfo {
  implicit val encoder: Encoder[SchemaInfo] = deriveEncoder
  implicit val decoder: Decoder[SchemaInfo] = deriveDecoder
}

final case class SchemaInfo(name: String, documentsCount: Long)
