package infra.exchange.coincheck.responses

final case class OrdersResponse(
  success: Boolean,
  id: Long,
  rate: String,
  amount: String,
  order_type: String,
  stop_less_rate: Option[String],
  pair: String,
  created_at: String
)
