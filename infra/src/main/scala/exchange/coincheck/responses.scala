package exchange.coincheck

final case class TransactionsResponse(
  id: Long,
  orderId: Long,
  createdAt: String,
  funds: Map[String, String],
  pair: String,
  rate: String,
  feeCurrency: String,
  liquidity: String,
  side: String
)
