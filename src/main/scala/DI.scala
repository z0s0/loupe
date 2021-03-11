import Config.Configs
import service.Layer.Services
import service.{Layer => ServiceLayer}
import zio.ZLayer
import zio.clock.Clock

object DI {
  val live: ZLayer[Any, Throwable, Services with Configs] =
    Config.live >>>
      ElasticClient.live >>>
      ServiceLayer.live ++ Config.live ++ Clock.live
}
