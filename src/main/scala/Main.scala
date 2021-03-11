import api.Search
import org.http4s.syntax.kleisli._
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import service.ElasticInfo
import service.Layer.Services
import zio._
import zio.clock.Clock
import zio.interop.catz._
import zio.interop.catz.implicits.ioTimer

object Main extends App {
  type AppEnv = Services with Clock with Has[ApiConfig]
  type AppTask[T] = RIO[AppEnv, T]

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val program = for {
      apiConf <- ZIO.access[Has[ApiConfig]](_.get)
      res <- ElasticInfo.hasIndex("movies")
      _ <- UIO(println(res))
      httpApp = Router("/" -> Search.routes).orNotFound
      _ <- runHttp(httpApp, apiConf)
    } yield ()

    program.provideCustomLayer(DI.live).exitCode
  }

  def runHttp[R <: Clock](httpApp: HttpApp[RIO[R, *]], apiConf: ApiConfig) = {
    type Task[T] = RIO[R, T]

    ZIO.runtime[R].flatMap { implicit rt =>
      BlazeServerBuilder[Task]
        .bindHttp(apiConf.port, "localhost")
        .withHttpApp(httpApp)
        .serve
        .compile[Task, Task, cats.effect.ExitCode]
        .drain
    }
  }
}
