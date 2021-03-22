import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.{Endpoint, endpoint, query, stringBody}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object Main extends App {
  val helloWorld: Endpoint[String, Unit, String, Any] =
    endpoint.get.in("hello").in(query[String]("name")).out(stringBody)

  // converting an endpoint to a route (providing server-side logic); extension method comes from imported packages
  val helloWorldRoute: Route = AkkaHttpServerInterpreter.toRoute(helloWorld)(
    name => Future.successful(Right(s"Hello, $name!"))
  )

  // starting the server
  implicit val actorSystem: ActorSystem = ActorSystem()
  import actorSystem.dispatcher

  val bindAndCheck =
    Http().newServerAt("localhost", 8080).bindFlow(helloWorldRoute).map { _ =>
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
