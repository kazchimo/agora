package infra.exchange.coincheck.impl

import domain.exchange.coincheck.CCOrder.CCOrderRate

final case class DryWriteCoincheckExchangeImpl(
  orderSettledInterval: Int,
  marketRate: CCOrderRate
) extends DryCoincheckExchangeImpl(marketRate) with PublicTransactions
    with Transactions with DryOrders with DryOpenOrders with DryCancelOrder
    with DryCancelStatus with DryBalance
