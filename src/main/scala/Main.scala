import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.requests.mappings.GetMappingRequest
import zio._
import zio.console.putStrLn
import com.sksamuel.elastic4s.zio.instances._
import com.sksamuel.elastic4s.ElasticDsl._
import service.Search.Search

object Main extends App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val program = for {
      searchService <- ZIO.access[Search](_.get)
      result <- searchService.find("movies", "star")
      _ <- UIO(println(result))
    } yield ()

    program.provideCustomLayer(DI.live).exitCode
  }
}
