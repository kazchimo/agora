package infra.exchange.coincheck.impl

import domain.exchange.coincheck.CoincheckExchange
import infra.exchange.coincheck.CoinCheckExchangeConfig

final case class CoinCheckExchangeImpl(conf: CoinCheckExchangeConfig)
    extends CoincheckExchange.Service
    with Transactions
    with Orders
    with AuthStrategy
    with PublicTransactions
