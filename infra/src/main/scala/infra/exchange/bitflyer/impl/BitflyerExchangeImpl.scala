package infra.exchange.bitflyer.impl

import domain.conf.{BFAccessKey, BFSecretKey}
import domain.exchange.bitflyer.BitflyerExchange

final case class BitflyerExchangeImpl(
  accessKey: BFAccessKey,
  secretKey: BFSecretKey
) extends BitflyerExchange.Service
    with AuthStrategy
    with ChildOrder
