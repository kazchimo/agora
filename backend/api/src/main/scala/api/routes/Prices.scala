package api.routes

import api.layers
import io.circe.generic.auto._
import sttp.capabilities.akka.AkkaStreams
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import usecase.liquid.WatchExecutionStreamUC
import zio._

import scala.concurrent.Future

private case class Response(hello: String)

object Prices {

  val ep = endpoint.get
    .in("prices").out(
      webSocketBody[String, CodecFormat.TextPlain, Response, CodecFormat.Json](
        AkkaStreams
      )
    ).serverLogic[Future] { _ =>
      Runtime.default.unsafeRunToFuture(
        WatchExecutionStreamUC.watch.provideCustomLayer(layers.all)
      )
    }

}
