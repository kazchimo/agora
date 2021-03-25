package infra.exchange.liquid.response

import domain.exchange.liquid.{LiquidOrder, Trade}
import lib.error.ClientDomainError
import lib.refined.{NonNegativeLong, PositiveDouble, PositiveLong}
import zio.IO

final private[liquid] case class OrderResponse(
  id: PositiveLong,
  price: PositiveDouble,
  quantity: PositiveDouble,
  status: String
) {
  import domain.exchange.liquid.LiquidOrder.{Id, Price, Quantity, Status}

  def toOrder: IO[ClientDomainError, LiquidOrder] = Status
    .withNameZio(status).map(
      LiquidOrder(Id(id), Price(price), Quantity(price), _)
    )
}

final private[liquid] case class TradeResponse(
  id: PositiveLong,
  status: String
) {
  import domain.exchange.liquid.Trade._

  def toTrade: IO[ClientDomainError, Trade] =
    Status.withNameZio(status).map(Trade(Id(id), _))
}

final private[liquid] case class PaginationContainer[R](
  models: Seq[R],
  current_page: NonNegativeLong,
  total_pages: NonNegativeLong
)
