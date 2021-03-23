package api

import api.routes.Prices
import cats.effect._
import cats.syntax.all._
import domain.AllEnv
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import sttp.client3._
import sttp.tapir.ztapir._
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import zio.{App, RIO, URIO, ZEnv, ZIO}
import zio.interop.catz._
import zio.clock.Clock

import scala.concurrent.ExecutionContext

object Main extends App {
  val helloWorld =
    endpoint.get.in("hello").in(query[String]("name")).out(stringBody)

  // mandatory implicits
  val ec: ExecutionContext                    = scala.concurrent.ExecutionContext.Implicits.global
  implicit val contextShift: ContextShift[IO] = IO.contextShift(ec)
  implicit val timer: Timer[IO]               = IO.timer(ec)

  val serve = ZIO
    .runtime[AllEnv].flatMap { implicit runtime =>
      BlazeServerBuilder(runtime.platform.executor.asEC)
        .bindHttp(8080, "localhost").withHttpApp(
          Router("/" -> Prices.routes).orNotFound
        ).serve.compile.drain
    }.provideCustomLayer(layers.all)

  override def run(args: List[String]) = serve.exitCode
}
