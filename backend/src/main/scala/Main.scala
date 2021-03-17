import cats.syntax.show._
import infra.conf.ConfImpl
import infra.exchange.{ExchangeImpl, IncreasingNonceImpl}
import lib.error._
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import usecase.coincheck.{
  CancelAllInCoincheckUC,
  SellAllCoinInCoincheckUC,
  TradeInDowMethodUC
}
import usecase.liquid.WatchProductStreamUC
import zio.logging.{LogLevel, Logging, log}
import zio.magic._
import zio.{ExitCode, URIO, ZEnv, ZIO}

object Main extends zio.App {
  override def run(args: List[String]): URIO[ZEnv, ExitCode] = app
    .foldM(
      {
        case e: Error => log.error(e.show)
        case e        => log.error(e.getMessage)
      },
      _ => ZIO.unit
    )
    .provideCustomMagicLayer(
      ConfImpl.layer,
      ExchangeImpl.coinCheckExchange,
      ExchangeImpl.liquidExchange,
      AsyncHttpClientZioBackend.layer(),
      Logging.console(logLevel = LogLevel.Info),
      IncreasingNonceImpl.layer(System.currentTimeMillis())
    )
    .exitCode

  val tradeInDow = TradeInDowMethodUC.trade(5, 3, 3)
  val sellAll    = SellAllCoinInCoincheckUC.sell(10)
  val cancelAll  = CancelAllInCoincheckUC.cancelAll
  val settleAll  = sellAll <&> cancelAll

  private val app =
    log.info("start") *> WatchProductStreamUC.watch *> log.info("end")
}
