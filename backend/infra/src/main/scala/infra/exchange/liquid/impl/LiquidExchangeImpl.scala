package infra.exchange.liquid.impl

import domain.exchange.liquid.LiquidExchange

final case class LiquidExchangeImpl()
    extends LiquidExchange.Service with ProductsStream