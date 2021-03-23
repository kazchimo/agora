package api.routes

import domain.AllEnv
import io.circe.generic.auto._
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.http4s.Http4sServerInterpreter
import zio._
import zio.interop.catz._

private case class Response(hello: String)

object Prices {

  val ep = endpoint.get
    .in("prices").out(
      webSocketBody[String, CodecFormat.TextPlain, Response, CodecFormat.Json](
        Fs2Streams[RIO[AllEnv, *]]
      )
    )

  val routes = Http4sServerInterpreter.toRoutes(ep)(_ =>
    RIO(Right(_.as(Response("accepted"))))
  )

}
