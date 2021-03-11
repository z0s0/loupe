package loupe

import loupe.Config.Configs
import loupe.service.Layer.Services
import loupe.service.{Layer => ServiceLayer}
import zio.ZLayer
import zio.clock.Clock

object DI {
  val live: ZLayer[Any, Throwable, Services with Configs] =
    Config.live >>>
      ElasticClient.live >>>
      ServiceLayer.live ++ Config.live ++ Clock.live
}
