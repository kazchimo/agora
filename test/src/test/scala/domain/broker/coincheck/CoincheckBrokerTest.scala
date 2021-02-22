package domain.broker.coincheck

import domain.conf.Conf
import domain.exchange.Nonce.Nonce
import domain.exchange.coincheck.CCOrder.CCOrderPair.BtcJpy
import domain.exchange.coincheck.CCOrder.{
  CCOrderCreatedAt,
  CCOrderId,
  CCOrderRate
}
import domain.exchange.coincheck.{CCOpenOrder, CCOrder, CoincheckExchange}
import infra.conf.ConfImpl
import infra.exchange.IncreasingNonceImpl
import sttp.client3.asynchttpclient.zio.{AsyncHttpClientZioBackend, SttpClient}
import zio.logging.Logging
import zio.test.Assertion.isFalse
import zio.test._
import zio.{ZEnv, ZIO}

object CoincheckBrokerTest extends DefaultRunnableSpec {
  override def spec = suite("#CoincheckBroker")(testM("#waitOrderSettled") {
    val id       = CCOrderId.unsafeFrom(1L)
    val id2      = CCOrderId.unsafeFrom(2L)
    val exchange = CoincheckExchange.stubLayer(openOrdersRes =
      ZIO.succeed(
        Seq(
          CCOpenOrder.buy(
            id,
            CCOrderRate.unsafeFrom(100d),
            BtcJpy,
            CCOrderCreatedAt.unsafeFrom("today")
          )
        )
      )
    )

    (for {
      successFiber <- CoincheckBroker().waitOrderSettled(id2).fork
      failFiber    <- CoincheckBroker().waitOrderSettled(id).fork
      _            <- successFiber.join
      done         <- failFiber.status.map(_.isDone)
    } yield assert(done)(isFalse))
      .provideSomeLayer[SttpClient with Conf with Logging with ZEnv with Nonce](
        exchange
      )
  }).provideCustomLayer(
    AsyncHttpClientZioBackend.stubLayer.orDie ++ ConfImpl.stubLayer ++ Logging.ignore ++ IncreasingNonceImpl
      .layer(0)
  )
}
