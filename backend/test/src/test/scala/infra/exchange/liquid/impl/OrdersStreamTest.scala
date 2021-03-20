package infra.exchange.liquid.impl

import domain.exchange.liquid.LiquidOrder._
import domain.exchange.liquid.OrderOnBook
import zio.test.Assertion.equalTo
import zio.test._

object OrdersStreamTest extends DefaultRunnableSpec {
  override def spec = suite("#ordersStream")(testM("#toLiquidOrders") {
    val data = """[["1.1","2.2"],["3.3","4.4"]]"""
    assertM(OrderBookStream.toLiquidOrders(data))(
      equalTo(
        List(
          OrderOnBook(Price.unsafeFrom(1.1), Quantity.unsafeFrom(2.2)),
          OrderOnBook(Price.unsafeFrom(3.3), Quantity.unsafeFrom(4.4))
        )
      )
    )
  })
}
