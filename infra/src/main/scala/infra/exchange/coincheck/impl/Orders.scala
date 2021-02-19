package infra.exchange.coincheck.impl

import domain.exchange.coincheck.CCOrder.CCOrderId
import domain.exchange.coincheck.{CCOrder, CCLimitOrderRequest}
import infra.InfraError
import infra.exchange.coincheck.Endpoints
import infra.exchange.coincheck.bodyconverter.CCOrderConverter._
import infra.exchange.coincheck.responses.{
  FailedOrdersResponse,
  OrdersResponse,
  SuccessOrdersResponse
}
import io.circe.syntax._
import sttp.client3.asynchttpclient.zio.{SttpClient, send}
import sttp.client3.circe.asJson
import sttp.client3.{UriContext, basicRequest}
import zio.{RIO, Task, ZEnv, ZIO}

private[coincheck] trait Orders extends AuthStrategy {
  self: CoinCheckExchangeImpl =>
  private def request(order: CCLimitOrderRequest) = for {
    h <- headers(Endpoints.orders, order.asJson.noSpaces)
  } yield basicRequest.post(uri"${Endpoints.orders}").contentType("application/json").body(order.asJson.noSpaces).headers(h).response(asJson[OrdersResponse])

  final override def orders(
    order: CCLimitOrderRequest
  ): RIO[SttpClient with ZEnv, CCOrder] = (for {
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
