package loupe.model.params

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

object CreateSchemaParams {
  implicit val jsonDecoder: Decoder[CreateSchemaParams] = deriveDecoder
  implicit val jsonEncoder: Encoder[CreateSchemaParams] = deriveEncoder
}
final case class CreateSchemaParams(name: String,
                                    lang: String,
                                    mappings: Map[String, String])
