package infra.exchange.bitflyer.impl
import domain.exchange.bitflyer.BFChildOrder
import infra.exchange.bitflyer.bodyconverter.BFChildOrderConverter._
import infra.exchange.bitflyer.responses.ChildOrderResponse
import io.circe.generic.auto._
import io.circe.syntax._
import lib.sttp.jsonRequest
import sttp.client3.UriContext
import sttp.client3.asynchttpclient.zio.{SttpClient, send}
import sttp.client3.circe.asJson
import sttp.model.Method.POST
import zio.console.{Console, putStrLn}
import zio.{RIO, ZIO}

private[bitflyer] trait ChildOrder { self: BitflyerExchangeImpl =>
  private val path = "/v1/me/sendchildorder"
  private val uri  = "https://api.bitflyer.com" + path

  private def request(order: BFChildOrder) =
    headers(POST.method, path, order.asJson.noSpaces).map { h =>
      jsonRequest
        .post(uri"$uri")
        .body(order.asJson.noSpaces)
        .headers(h)
        .response(asJson[ChildOrderResponse])
    }

  final override def childOrder(
    order: BFChildOrder
  ): RIO[SttpClient with Console, Unit] = for {
    req <- request(order)
    _   <- putStrLn(req.body.toString)
    res <- send(req)
    _   <- ZIO.fromEither(res.body)
  } yield ()
}
