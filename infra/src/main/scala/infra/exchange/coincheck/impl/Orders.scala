package infra.exchange.coincheck.impl

import domain.exchange.coincheck.CCOrder
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
  private def request(order: CCOrder) = for {
    h <- headers(Endpoints.orders, order.asJson.noSpaces)
  } yield basicRequest.post(uri"${Endpoints.orders}").contentType("application/json").body(order.asJson.noSpaces).headers(h).response(asJson[OrdersResponse])

  final override def orders(order: CCOrder): RIO[SttpClient with ZEnv, Unit] =
    for {
      req  <- request(order)
      res  <- send(req)
      body <- ZIO.fromEither(res.body)
      r    <- body match {
                case _: SuccessOrdersResponse    => Task.succeed(())
                case FailedOrdersResponse(error) => Task.fail(
                    InfraError(
                      s"failed to order: order=${order.toString} error=$error"
                    )
                  )
              }
    } yield r

}
