package api

import api.routes.Prices
import cats.effect._
import domain.AllEnv
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import sttp.tapir.ztapir._
import zio.interop.catz._
import zio.{App, ZIO}

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
