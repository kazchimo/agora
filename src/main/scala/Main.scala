import cats.syntax.show._
import infra.conf.ConfImpl
import infra.exchange.{ExchangeImpl, IncreasingNonceImpl}
import lib.error._
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import usecase.{SellAllCoinInCoincheckUC, TradeInDowMethodUC}
import zio.logging.{LogLevel, Logging, log}
import zio.magic._
import zio.{ExitCode, URIO, ZEnv, ZIO}

object Main extends zio.App {
  override def run(args: List[String]): URIO[ZEnv, ExitCode] = app
    .foldM(
      {
        case e: Error => log.error(e.show) *> sellAll
        case e        => log.error(e.getMessage) *> sellAll
      },
      _ => ZIO.unit
    )
    .provideCustomMagicLayer(
      ConfImpl.layer,
      ExchangeImpl.coinCheckExchange,
      AsyncHttpClientZioBackend.layer(),
      Logging.console(logLevel = LogLevel.Debug),
      IncreasingNonceImpl.layer(System.currentTimeMillis())
    )
    .exitCode

  val tradeInDow = TradeInDowMethodUC.trade(5, 3, 3)
  val sellAll    = SellAllCoinInCoincheckUC.sell(10)

  private val app = log.info("start") *> sellAll *> log.info("end")
}
