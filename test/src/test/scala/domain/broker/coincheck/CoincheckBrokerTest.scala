package domain.broker.coincheck

import domain.conf.Conf
import domain.exchange.coincheck.CCOrder.CCOrderId
import domain.exchange.coincheck.{CCOrder, CoincheckExchange}
import infra.conf.ConfImpl
import sttp.client3.asynchttpclient.zio.{AsyncHttpClientZioBackend, SttpClient}
import zio.ZIO
import zio.test.Assertion.isFalse
import zio.test._

object CoincheckBrokerTest extends DefaultRunnableSpec {
  override def spec = suite("#CoincheckBroker")(testM("#waitOrderSettled") {
    val id       = CCOrderId.unsafeFrom(1L)
    val id2      = CCOrderId.unsafeFrom(2L)
    val exchange =
      CoincheckExchange.stubLayer(openOrdersRes = ZIO.succeed(Seq(CCOrder(id))))

    (for {
      successFiber <- CoincheckBroker().waitOrderSettled(id2).fork
      failFiber    <- CoincheckBroker().waitOrderSettled(id).fork
      _            <- successFiber.join
      done         <- failFiber.status.map(_.isDone)
    } yield assert(done)(isFalse))
      .provideSomeLayer[SttpClient with Conf](exchange)
  }).provideCustomLayer(
    AsyncHttpClientZioBackend.stubLayer.orDie ++ ConfImpl.stubLayer
  )
}
