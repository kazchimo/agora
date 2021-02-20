package infra.exchange.coincheck.impl

import domain.exchange.coincheck.CoincheckExchange

final case class CoinCheckExchangeImpl()
    extends CoincheckExchange.Service with Transactions with Orders
    with AuthStrategy with PublicTransactions with OpenOrders with CancelOrder
    with CancelStatus
