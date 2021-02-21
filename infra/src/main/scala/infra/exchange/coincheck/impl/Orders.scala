package infra.exchange.coincheck.impl

import domain.conf.Conf
import domain.exchange.coincheck.CCOrder.CCOrderId
import domain.exchange.coincheck.{CCOrder, CCOrderRequest, CoincheckExchange}
import infra.InfraError
import infra.exchange.coincheck.Endpoints
import infra.exchange.coincheck.bodyconverter.CCOrderConverter._
import infra.exchange.coincheck.responses.{
  FailedOrdersResponse,
  OrdersResponse,
  SuccessOrdersResponse
}
import io.circe.syntax._
import lib.sttp.jsonRequest
import sttp.client3.UriContext
import sttp.client3.asynchttpclient.zio.{SttpClient, send}
import sttp.client3.circe.asJson
import zio.{RIO, Task, ZEnv, ZIO}
import domain.exchange.coincheck.CCOrderRequest._

private[coincheck] trait Orders extends AuthStrategy {
  self: CoincheckExchange.Service =>
  private def request(order: CCOrderRequest[_ <: CCOrderType]) =
    headers(Endpoints.orders, order.asJson.noSpaces).map(h =>
      jsonRequest
        .post(uri"${Endpoints.orders}").body(order.asJson.noSpaces).headers(
          h
        ).response(asJson[OrdersResponse])
    )

  final override def orders(
    order: CCOrderRequest[_ <: CCOrderType]
  ): RIO[SttpClient with ZEnv with Conf, CCOrder] = (for {
    req  <- request(order)
    res  <- send(req)
    body <- ZIO.fromEither(res.body)
    r    <- body match {
              case r: SuccessOrdersResponse    => CCOrderId(r.id).map(CCOrder(_))
              case FailedOrdersResponse(error) => Task.fail(
                  InfraError(
                    s"failed to order: order=${order.toString} error=$error"
                  )
                )
            }
  } yield r).retryN(3)

}
