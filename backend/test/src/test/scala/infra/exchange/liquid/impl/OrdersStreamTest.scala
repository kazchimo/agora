package infra.exchange.liquid.impl

import domain.exchange.liquid.LiquidOrder
import domain.exchange.liquid.LiquidOrder._
import zio.test.Assertion.equalTo
import zio.test._

object OrdersStreamTest extends DefaultRunnableSpec {
  override def spec = suite("#ordersStream")(testM("#toLiquidOrders") {
    val data = """[["1.1","2.2"],["3.3","4.4"]]"""
    assertM(OrdersStream.toLiquidOrders(data))(
      equalTo(
        List(
          LiquidOrder(Price.unsafeFrom(1.1), Quantity.unsafeFrom(2.2)),
          LiquidOrder(Price.unsafeFrom(3.3), Quantity.unsafeFrom(4.4))
        )
      )
    )
  })
}
