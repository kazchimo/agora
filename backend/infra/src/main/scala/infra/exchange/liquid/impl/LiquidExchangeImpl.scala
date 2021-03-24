package infra.exchange.liquid.impl

import domain.exchange.liquid.LiquidExchange

final case class LiquidExchangeImpl()
    extends LiquidExchange.Service with ProductsStream with ExecutionStream
    with OrderBookStream with CreateOrder with GetOrder with CancelOrder
    with OrdersStream with TradesStream
