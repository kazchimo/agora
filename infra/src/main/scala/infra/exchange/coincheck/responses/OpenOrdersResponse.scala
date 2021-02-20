package infra.exchange.coincheck.responses

import cats.syntax.functor._
import io.circe.Decoder
import io.circe.generic.auto._

sealed trait OpenOrdersResponse extends CoincheckResponse

object OpenOrdersResponse {
  implicit val openOrdersResponseDecoder: Decoder[OpenOrdersResponse] =
    List[Decoder[OpenOrdersResponse]](
      Decoder[SuccessOpenOrdersResponse].widen,
      Decoder[FailedOpenOrdersResponse].widen
    ).reduceLeft(_ or _)
}

final case class SuccessOpenOrdersResponse(orders: Seq[OrderContent])
    extends OpenOrdersResponse with SuccessCoincheckResponse

final case class OrderContent(
  id: Long,
  rate: Option[Double],
  pending_amount: Option[Double],
  pending_market_buy_amount: Option[Double],
  order_type: String,
  stop_loss_rate: Option[Double],
  pair: String,
  created_at: String
)

final case class FailedOpenOrdersResponse(error: String)
    extends OpenOrdersResponse with FailedCoincheckResponse
