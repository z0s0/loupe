package loupe.model.errors

import io.circe.{Decoder, Encoder}
import io.circe.syntax.EncoderOps
import io.circe.generic.semiauto.deriveDecoder

import scala.util.control.NoStackTrace

sealed trait ClientError extends NoStackTrace
final case class Invalid(errors: List[String]) extends ClientError
case object NotFound extends ClientError
case object SomethingWentWrong extends ClientError

object ClientError {
  implicit val jsonEncoder: Encoder[ClientError] = Encoder.instance {
    case Invalid(errors) =>
      Map("errors" -> errors).asJson
    case NotFound =>
      Map("errors" -> List("not found")).asJson
    case SomethingWentWrong =>
      Map("errors" -> List("something went wrong")).asJson
  }
  implicit val jsonDecoder: Decoder[ClientError] = deriveDecoder
}
