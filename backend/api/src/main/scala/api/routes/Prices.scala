package api.routes

import domain.AllEnv
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.http4s.Http4sServerInterpreter
import usecase.api.{ExecutedOrder, StreamPricesUC}
import zio._
import zio.interop.catz._
import zio.stream.interop.fs2z._
import cats.syntax.either._
import domain.exchange._
import fs2.Pipe
import io.circe.{Encoder, Json}
import io.circe.syntax._

object Prices {
  implicit val encoder: Encoder[ExecutedOrder[Exchange]] =
    Encoder.instance { r =>
      r.exchange match {
        case _: Coincheck =>
          Json.obj("coincheck" -> r.price.asJson, "time" -> r.time.asJson)
        case _: Liquid    =>
          Json.obj("liquid" -> r.price.asJson, "time" -> r.time.asJson)
      }
    }

  val ep = endpoint.get
    .in("prices").out(
      webSocketBody[String, CodecFormat.TextPlain, Json, CodecFormat.Json](
        Fs2Streams[RIO[AllEnv, *]]
      )
    )

  val pipe: RIO[AllEnv, Pipe[RIO[AllEnv, *], String, Json]] =
    StreamPricesUC.getStream.map(_.toFs2Stream.map(_.asJson)).map(a => _ => a)

  val routes = Http4sServerInterpreter.toRoutes(ep)(_ => pipe.map(_.asRight))

}
