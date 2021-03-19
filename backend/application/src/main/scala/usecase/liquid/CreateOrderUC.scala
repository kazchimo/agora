package usecase.liquid

import domain.exchange.liquid.LiquidOrder.{OrderType, Side}
import domain.exchange.liquid.{LiquidExchange, LiquidOrderRequest}

object CreateOrderUC {
  def create[O <: OrderType, S <: Side](order: LiquidOrderRequest[O, S]) = for {
    _ <- LiquidExchange.createOrder(order)
  } yield ()
}
