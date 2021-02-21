package infra.exchange.coincheck.impl

import domain.exchange.coincheck.CCOrderRequest
import domain.exchange.coincheck.CCOrderRequest.CCOrderRequestRate
import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

object DryCoincheckExchangeImplTest extends DefaultRunnableSpec {
  private val marketRate = CCOrderRequestRate.unsafeFrom(10)

  override def spec = suite("DryCoincheckExchangeImpl")(
    suite("OrderCache")(
      testM("submitOrder") {
        val cache = OrderCache(marketRate)

        for {
          req   <- CCOrderRequest.limitBuy(1, 1)
          order <- ZIO.effect(cache.submitOrder(req))
        } yield assert(cache.openOrders)(equalTo(Seq(order)))
      },
      testM("cancelOrder") {
        val cache = OrderCache(marketRate)

        for {
          req   <- CCOrderRequest.limitBuy(1, 1)
          order <- ZIO.effect(cache.submitOrder(req))
          _      = cache.cancelOrder(order.id)
        } yield assert(cache.openOrders)(equalTo(Seq()))
      },
      testM("closeOrder") {
        val buy = for {
          req   <- CCOrderRequest.limitBuy(2, 1)
          cache  = OrderCache(marketRate)
          order <- ZIO.effect(cache.submitOrder(req))
          _     <- cache.closeOrder(order.id)
        } yield assert(cache.btc)(equalTo(1d)) && assert(cache.jpy)(equalTo(-2d))

        val sell = for {
          req   <- CCOrderRequest.limitSell(2, 1)
          cache  = OrderCache(marketRate)
          order <- ZIO.effect(cache.submitOrder(req))
          _     <- cache.closeOrder(order.id)
        } yield assert(cache.btc)(equalTo(-1d)) && assert(cache.jpy)(equalTo(2d))

        val marketBuy = for {
          req   <- CCOrderRequest.marketBuy(20)
          cache  = OrderCache(marketRate)
          order <- ZIO.effect(cache.submitOrder(req))
          _     <- cache.closeOrder(order.id)
        } yield assert(cache.btc)(equalTo(2d)) && assert(cache.jpy)(equalTo(-20d))

        val marketSell = for {
          req   <- CCOrderRequest.marketSell(1)
          cache  = OrderCache(marketRate)
          order <- ZIO.effect(cache.submitOrder(req))
          _     <- cache.closeOrder(order.id)
        } yield assert(cache.btc)(equalTo(-1d)) && assert(cache.jpy)(equalTo(10d))

        for {
          b  <- buy
          s  <- sell
          mb <- marketBuy
          ms <- marketSell
        } yield b && s && mb && ms
      }
    )
  )
}
