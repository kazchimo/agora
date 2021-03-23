package api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import api.routes.Prices
import sttp.tapir._
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object Main extends App {
  val helloEp = endpoint.get
    .in("hello").in(query[String]("name")).out(stringBody).serverLogic[Future](
      name => Future.successful(Right(s"Hello, $name!"))
    )

  implicit val actorSystem: ActorSystem = ActorSystem()

  import actorSystem.dispatcher

  val routes = AkkaHttpServerInterpreter.toRoute(List(Prices.ep, helloEp))

  val bindAndCheck =
    Http().newServerAt("localhost", 8080).bindFlow(routes).map { _ =>
      println("Go to: http://localhost:8080")
      println("Press any key to exit ...")
      scala.io.StdIn.readLine()
    }

  Await.result(
    bindAndCheck.transformWith { r =>
      actorSystem.terminate().transform(_ => r)
    },
    Duration.Inf
  )
}