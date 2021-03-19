package infra.exchange.liquid.response

import domain.exchange.liquid.LiquidOrder
import domain.exchange.liquid.LiquidOrder.{Id, Price, Quantity, Status}
import lib.error.ClientDomainError
import lib.refined.{PositiveDouble, PositiveLong}
import zio.IO

final private[liquid] case class OrderResponse(
  id: PositiveLong,
  price: PositiveDouble,
  quantity: PositiveDouble,
  status: String
) {
  def toOrder: IO[ClientDomainError, LiquidOrder] = Status
    .withNameZio(status).map(
      LiquidOrder(Id(id), Price(price), Quantity(price), _)
    )
}
