package infra.exchange.coincheck.impl

import domain.exchange.coincheck.CCOrder
import infra.InfraError
import infra.exchange.coincheck.Endpoints
import infra.exchange.coincheck.bodyconverter.CCOrderConverter._
import infra.exchange.coincheck.responses.OrdersResponse
import io.circe.syntax._
import sttp.client3.asynchttpclient.zio.{send, SttpClient}
import sttp.client3.circe.asJson
import sttp.client3.{basicRequest, UriContext}
import zio.{RIO, Task, ZIO}

import scala.annotation.nowarn

private[exchange] trait Orders extends AuthStrategy {
  self: CoinCheckExchangeImpl =>
  @nowarn private def request(order: CCOrder) = for {
    h <- headers(Endpoints.orders, order.asJson.noSpaces)
  } yield basicRequest
    .post(uri"${Endpoints.orders}")
    .contentType("application/json")
    .body(order.asJson.noSpaces)
    .headers(h)
    .response(asJson[OrdersResponse])

  override final def orders(order: CCOrder): RIO[SttpClient, Unit] = for {
    req  <- request(order)
    _     = println(req.body)
    res  <- send(req)
    body <- ZIO.fromEither(res.body)
    r    <- if (body.success) Task.succeed(())
            else
              Task.fail(InfraError(s"failed to order: ${order.toString}"))
  } yield r

}
