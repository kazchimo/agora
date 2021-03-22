package routes

import akka.stream.scaladsl.Flow
import io.circe.generic.auto._
import sttp.capabilities.akka.AkkaStreams
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

import scala.concurrent.Future

private case class Response(hello: String)

object Prices {

  val ep = endpoint.get
    .in("prices").out(
      webSocketBody[String, CodecFormat.TextPlain, Response, CodecFormat.Json](
        AkkaStreams
      )
    ).serverLogic[Future] { _ =>
      Future.successful(Right(Flow.fromFunction(in => Response(in))))
    }

}
