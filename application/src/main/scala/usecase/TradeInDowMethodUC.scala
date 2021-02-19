package usecase

import domain.exchange.coincheck.CCOrder.CCOrderId
import domain.exchange.coincheck.CCOrderRequest.{
  CCOrderRequestAmount,
  CCOrderRequestRate
}
import domain.exchange.coincheck.{
  CCBuyRequest,
  CCSellRequest,
  CoincheckExchange
}
import domain.strategy.DowMethod
import zio.Ref
import zio.logging.log

final case class TradingState(
  onLong: Boolean,
  tradeSummary: Double,
  lastBuyRate: Double
) {
  def toLongPosition: TradingState = this.copy(true)

  def toNeutralPosition: TradingState = this.copy(false)

  def buyAt(rate: Double): TradingState = this.copy(lastBuyRate = rate)

  def addSummary(d: Double): TradingState =
    this.copy(tradeSummary = d + this.tradeSummary)
}

object TradeInDowMethodUC {
  val jpy = 100000

  private def waitOrderSettled(id: CCOrderId) =
    CoincheckExchange.openOrders.repeatWhile(_.map(_.id).contains(id))

  def trade(aggCount: Int, buyContinuous: Int, sellContinuous: Int) = for {
    _                  <- log.info("Buying in Dow method start...")
    transactionsStream <- CoincheckExchange.publicTransactions
    tradingStateRef    <- Ref.make(TradingState(false, 0, 0))
    signalStream       <- DowMethod(aggCount, buyContinuous, sellContinuous)
                            .signal(transactionsStream)
    _                  <- signalStream.foreach { signal =>
                            for {
                              tradingState <- tradingStateRef.get
                              _            <- {
                                for {
                                  _      <- log.info("Buy!")
                                  rate   <- CCOrderRequestRate(signal.at.toLong)
                                  amount <- CCOrderRequestAmount(jpy / signal.at)
                                  order  <- CoincheckExchange.orders(CCBuyRequest(rate, amount))
                                  _      <- waitOrderSettled(order.id)
                                  _      <- tradingStateRef.update(_.toLongPosition.buyAt(signal.at))
                                } yield ()
                              }.when(signal.shouldBuy & !tradingState.onLong)
                              _            <- {
                                for {
                                  _            <- log.info("Sell!")
                                  rate         <- CCOrderRequestRate(signal.at.toLong)
                                  amount       <- CCOrderRequestAmount(jpy / tradingState.lastBuyRate)
                                  order        <- CoincheckExchange.orders(CCSellRequest(rate, amount))
                                  _            <- waitOrderSettled(order.id)
                                  tradingState <- tradingStateRef.get
                                  profit        =
                                    amount.value.value * (signal.at - tradingState.lastBuyRate)
                                  summary      <- tradingStateRef.updateAndGet(
                                                    _.toNeutralPosition.addSummary(profit)
                                                  )
                                  _            <-
                                    log.info(
                                      s"Profit: ${profit.toString} Total: ${summary.tradeSummary.toString}"
                                    )
                                } yield ()
                              }.when(signal.shouldSell & tradingState.onLong)
                            } yield ()
                          }
  } yield ()
}
