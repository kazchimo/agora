package infra.exchange.coincheck.responses

sealed trait OrdersResponse extends CoincheckResponse

final case class SuccessOrdersResponse(
  id: Long,
  rate: String,
  amount: String,
  order_type: String,
  stop_less_rate: Option[String],
  pair: String,
  created_at: String
) extends OrdersResponse
    with SuccessCoincheckResponse

final case class FailedOrdersResponse(error: String)
    extends OrdersResponse
    with FailedCoincheckResponse
