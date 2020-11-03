package infra.exchange.coincheck.responses

import domain.currency.{Currency, TickerSymbol}
import domain.exchange.coincheck.CCTransaction
import domain.exchange.coincheck.CCTransaction.{
  Buy,
  CCTraCreatedAt,
  CCTraId,
  CCTraRate,
  CCTraSide,
  Sell
}
import io.scalaland.chimney.Transformer
import zio.{Task, ZIO}

final case class TransactionsResponse(
  success: Boolean,
  transactions: List[TransactionResponse]
)

final case class TransactionResponse(
  id: Long,
  order_id: Long,
  created_at: String,
  funds: Map[String, String],
  pair: String,
  rate: String,
  fee_currency: Option[String],
  liquidity: String,
  side: String
) {
  def dId: Task[CCTraId] = CCTraId(id)

  def dSide: Task[CCTraSide] = CCTraSide(side)

  private val currencies = pair.split("_")

  def sellCurrency: Task[Currency] =
    for {
      s         <- dSide
      rawTicker <- Task.effect {
                     s match {
                       case Buy  => currencies(1)
                       case Sell => currencies.head
                     }
                   }
      qua       <- Task.effect(funds(rawTicker).toDouble)
      ticker    <- TickerSymbol(rawTicker)
    } yield Currency(ticker, qua)

  def buyCurrency: Task[Currency] = for {
    s         <- dSide
    rawTicker <- Task.effect {
                   s match {
                     case Buy  => currencies.head
                     case Sell => currencies(1)
                   }
                 }
    qua       <- Task.effect(funds(rawTicker).toDouble)
    ticker    <- TickerSymbol(rawTicker)
  } yield Currency(ticker, qua)

  def dRate: Task[CCTraRate] = ZIO.effect(rate.toDouble).flatMap(CCTraRate(_))
}

object TransactionResponse {
  implicit val toTransactionTransformer
    : Transformer[TransactionResponse, Task[CCTransaction]] = res => {
    for {
      id        <- CCTraId(res.id)
      sellCur   <- res.sellCurrency
      buyCur    <- res.buyCurrency
      side      <- CCTraSide(res.side)
      createdAt <- CCTraCreatedAt(res.created_at)
      rate      <- res.dRate
    } yield CCTransaction(id, sellCur, buyCur, side, createdAt, rate)
  }
}
