package loupe.model

import java.io.File

import io.circe.Json
import sttp.model.Part

final case class UploadData(data: Part[File])
