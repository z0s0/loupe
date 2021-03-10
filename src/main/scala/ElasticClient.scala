import com.sksamuel.elastic4s.{ElasticProperties, ElasticClient => Client}
import com.sksamuel.elastic4s.http.JavaClient
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClientBuilder.RequestConfigCallback
import zio.{Has, URLayer, ZLayer}

object ElasticClient {
  val live: URLayer[Has[ElasticConfig], Has[Client]] = ZLayer.fromService {
    conf =>
      val props = ElasticProperties(conf.uris.mkString(","))

      conf.auth match {
        case Some(auth) =>
          val provider = {
            val credentials =
              new UsernamePasswordCredentials(auth.username, auth.password)
            val provider = new BasicCredentialsProvider
            provider.setCredentials(AuthScope.ANY, credentials)
            provider
          }

          Client(
            JavaClient(
              props,
              new RequestConfigCallback {
                override def customizeRequestConfig(
                  requestConfigBuilder: RequestConfig.Builder
                ): RequestConfig.Builder = requestConfigBuilder
              },
              (httpClientBuilder: HttpAsyncClientBuilder) =>
                httpClientBuilder.setDefaultCredentialsProvider(provider)
            )
          )

        case None =>
          Client(JavaClient(props))
      }
  }
}
