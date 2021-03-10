import pureconfig.ConfigSource
import pureconfig.generic.auto._
import zio.{Has, Layer, Task, ZLayer}

final case class Config(elasticConfig: ElasticConfig)
final case class ElasticConfig(uris: List[String], auth: Option[Auth] = None)
final case class Auth(username: String, password: String)

object Config {
  type Configs = Has[ElasticConfig]

  val live: Layer[Throwable, Configs] =
    Task
      .effect(ConfigSource.default.loadOrThrow[Config])
      .map(conf => conf.elasticConfig)
      .toLayer

}
