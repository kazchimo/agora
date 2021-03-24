package domain.exchange.liquid

final case class GetTradesParams(
  productId: Option[LiquidProduct.Id] = None,
  fundingCurrency: Option[FundingCurrency] = None,
  status: Option[Trade.Status] = None,
  side: Option[Trade.Side] = None,
  tradingType: Option[Trade.TradingType] = None
)
