package infra.exchange.coincheck.impl

import domain.conf.{CCEAccessKey, CCESecretKey}
import domain.exchange.coincheck.CoincheckExchange

final case class CoinCheckExchangeImpl(
  accessKey: CCEAccessKey,
  secretKey: CCESecretKey
) extends CoincheckExchange.Service with Transactions with Orders
    with AuthStrategy with PublicTransactions with OpenOrders with CancelOrder
