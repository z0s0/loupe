package loupe

import pureconfig.ConfigSource
import pureconfig.generic.auto._
import zio.{Has, Layer, Task, ZLayer}

final case class Config(elasticConfig: ElasticConfig, apiConfig: ApiConfig)

final case class ApiConfig(port: Int)
final case class ElasticConfig(uris: List[String], auth: Option[Auth] = None)
final case class Auth(username: String, password: String)

object Config {
  type Configs = Has[ElasticConfig] with Has[ApiConfig]

  val live: Layer[Throwable, Configs] =
    ZLayer.fromEffectMany(
      Task
        .effect(ConfigSource.default.loadOrThrow[Config])
        .map(conf => Has(conf.elasticConfig) ++ Has(conf.apiConfig))
    )
}
