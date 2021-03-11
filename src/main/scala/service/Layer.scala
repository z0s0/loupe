package service

import com.sksamuel.elastic4s.ElasticClient
import service.ElasticInfo.ElasticInfo
import zio.{Has, URLayer}

object Layer {
  type Services = ElasticInfo with Search.Search

  val live
    : URLayer[Has[ElasticClient], Services] = ElasticInfo.live ++ Search.live
}
