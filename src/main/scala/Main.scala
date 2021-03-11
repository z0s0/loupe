import api.{Docs, Schemas, Search}
import cats.syntax.all._
import org.http4s.syntax.kleisli._
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import service.Layer.Services
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.swagger.http4s.SwaggerHttp4s
import zio._
import zio.clock.Clock
import sttp.tapir.ztapir._
import zio.interop.catz._

object Main extends App {
  type AppEnv = Services with Clock with Has[ApiConfig]
  type AppTask[T] = RIO[AppEnv, T]

  val searchRoutes = Search.routes.map(_.widen[AppEnv])
  val schemasRoutes = Schemas.routes.map(_.widen[AppEnv])

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val routes =
      ZHttp4sServerInterpreter.from(searchRoutes ++ schemasRoutes).toRoutes

    val program = for {
      apiConf <- ZIO.access[Has[ApiConfig]](_.get)
      _ <- runHttp(routes, apiConf)
    } yield ()

    program.provideCustomLayer(DI.live).exitCode
  }

  def runHttp[R <: Clock](routes: HttpRoutes[RIO[R, *]], apiConf: ApiConfig) = {
    type Task[T] = RIO[R, T]

    ZIO.runtime[R].flatMap { implicit rt =>
      val swagger = new SwaggerHttp4s(Docs.yaml).routes[Task]

      BlazeServerBuilder[Task]
        .bindHttp(apiConf.port, "localhost")
        .withHttpApp(Router("/" -> (routes <+> swagger)).orNotFound)
        .serve
        .compile[Task, Task, cats.effect.ExitCode]
        .drain
    }
  }
}
