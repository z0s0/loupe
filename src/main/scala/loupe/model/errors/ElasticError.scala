package loupe.model.errors

import io.circe.Encoder
import io.circe.syntax.EncoderOps

import scala.util.control.NoStackTrace

sealed trait ElasticError extends NoStackTrace
final case class Conflict(reason: String) extends ElasticError
final case class Disaster(reason: String) extends ElasticError

object ElasticError {
  implicit val jsonEncoder: Encoder[ElasticError] = Encoder.instance {
    case Conflict(reason) => Map("errors" -> List(reason)).asJson
    case Disaster(reason) => Map("errors" -> List(reason)).asJson
  }
}
