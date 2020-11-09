package infra.exchange.bitflyer.impl

import domain.conf.{BFAccessKey, BFSecretKey}
import domain.exchange.bitflyer.BitflyerExchange

final case class BitflyerExchangeImpl(
  secretKey: BFSecretKey,
  accessKey: BFAccessKey
) extends BitflyerExchange.Service
    with AuthStrategy
    with ChildOrder
