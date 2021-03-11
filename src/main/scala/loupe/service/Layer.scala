package loupe.service

import com.sksamuel.elastic4s.ElasticClient
import ElasticManager.ElasticManager
import zio.{Has, URLayer}

object Layer {
  type Services = ElasticManager with Search.Search

  val live
    : URLayer[Has[ElasticClient], Services] = ElasticManager.live ++ Search.live
}
