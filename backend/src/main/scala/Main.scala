import domain.exchange.liquid.LiquidOrder.{Price, Quantity}
import domain.exchange.liquid.LiquidOrderRequest
import domain.exchange.liquid.LiquidProduct.btcJpyId
import eu.timepit.refined.auto._
import infra.conf.ConfImpl
import infra.exchange.{ExchangeImpl, IncreasingNonceImpl}
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import usecase.coincheck.{
  CancelAllInCoincheckUC,
  SellAllCoinInCoincheckUC,
  TradeInDowMethodUC
}
import usecase.liquid.{SettleWorstTrade, TradeHeadSpreadWithLeveraged}
import zio.duration._
import zio.logging.{LogLevel, Logging, log}
import zio.magic._
import zio.{ExitCode, URIO, ZEnv, ZIO}

object Main extends zio.App {
  override def run(args: List[String]): URIO[ZEnv, ExitCode] = app
    .foldCauseM(c => log.error(c.prettyPrint), _ => ZIO.unit)
    .provideCustomMagicLayer(
      ConfImpl.layer,
      ExchangeImpl.coinCheckExchange,
      ExchangeImpl.liquidExchange,
      AsyncHttpClientZioBackend.layer(),
      Logging.console(logLevel = LogLevel.Debug),
      IncreasingNonceImpl.layer(System.currentTimeMillis())
    )
    .exitCode

  val tradeInDow = TradeInDowMethodUC.trade(5, 3, 3)
  val sellAll    = SellAllCoinInCoincheckUC.sell(10)
  val cancelAll  = CancelAllInCoincheckUC.cancelAll
  val settleAll  = sellAll <&> cancelAll

  val liquidOrder       = LiquidOrderRequest.limitBuy(
    btcJpyId,
    Quantity.unsafeFrom(0.001),
    Price.unsafeFrom(6369581d)
  )
  val liquidSpreadTrade = TradeHeadSpreadWithLeveraged.trade(20)

  private val app = log.info("start") *> (liquidSpreadTrade &> SettleWorstTrade
    .settle(30.seconds)) *> log.info("end")
}
