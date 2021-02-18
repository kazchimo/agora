package infra.exchange.coincheck.responses

sealed trait OpenOrdersResponse extends CoincheckResponse

final case class SuccessOpenOrdersResponse(orders: Seq[OrderContent])
    extends OpenOrdersResponse with SuccessCoincheckResponse

final case class OrderContent(
  id: Long,
  rate: Option[String],
  pending_amount: Option[Double],
  pending_market_buy_amount: Option[Double],
  order_type: String,
  stop_loss_rate: Option[Double],
  pair: String,
  created_at: String
)

final case class FailedOpenOrdersResponse(error: String)
    extends OpenOrdersResponse with FailedCoincheckResponse
