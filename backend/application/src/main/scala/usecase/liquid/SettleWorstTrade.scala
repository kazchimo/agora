package usecase.liquid

import domain.exchange.liquid.FundingCurrency.Jpy
import domain.exchange.liquid.LiquidProduct.btcJpyId
import domain.exchange.liquid.Pagination.Limit
import domain.exchange.liquid.Trade.Status.Open
import domain.exchange.liquid.Trade.TradingType.Cfd
import domain.exchange.liquid.{GetTradesParams, LiquidExchange}
import eu.timepit.refined.auto._
import zio.{Schedule, ZIO}
import zio.duration.Duration

object SettleWorstTrade {

  def settle(interval: Duration) = {
    val param = GetTradesParams(
      Some(btcJpyId),
      Some(Jpy),
      Some(Open),
      None,
      Some(Cfd),
      Some(Limit(1000))
    )

    (for {
      trades    <- LiquidExchange.getTrades(param)
      worstTrade = trades.minByOption(_.pnl.value)
      _         <- worstTrade match {
                     case Some(t) => LiquidExchange.closeTrade(t.id)
                     case None    => ZIO.unit
                   }
    } yield ()).repeat(Schedule.fixed(interval))
  }
}
