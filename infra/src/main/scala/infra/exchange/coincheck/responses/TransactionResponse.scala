package infra.exchange.coincheck.responses

import cats.syntax.functor._
import cats.syntax.traverse._
import domain.currency.{Currency, TickerSymbol}
import domain.exchange.coincheck.CCTransaction
import domain.exchange.coincheck.CCTransaction.CCTraSide._
import domain.exchange.coincheck.CCTransaction._
import infra.InfraError
import io.circe.Decoder
import io.circe.generic.auto._
import io.scalaland.chimney.Transformer
import io.scalaland.chimney.dsl._
import zio.interop.catz.core._
import zio.{Task, ZIO}

sealed trait TransactionsResponse extends CoincheckResponse

object TransactionsResponse {
  implicit val toTransactionsTransformer
    : Transformer[TransactionsResponse, Task[List[CCTransaction]]] = {
    case SuccessTransactionsResponse(transactions) =>
      transactions.traverse(_.transformInto[Task[CCTransaction]])
    case FailedTransactionsResponse(error)         =>
      Task.fail(InfraError(s"occurred error while request: $error"))
  }

  implicit val transactionsResponseDecoder: Decoder[TransactionsResponse] =
    List[Decoder[TransactionsResponse]](
      Decoder[SuccessTransactionsResponse].widen,
      Decoder[FailedTransactionsResponse].widen
    ).reduceLeft(_ or _)

}

final case class SuccessTransactionsResponse(
  transactions: List[TransactionResponse]
) extends TransactionsResponse with SuccessCoincheckResponse

final case class FailedTransactionsResponse(error: String)
    extends TransactionsResponse with FailedCoincheckResponse

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
  def dSide: Task[CCTraSide] = ZIO.fromEither(CCTraSide.withNameEither(side))

  private val currencies = pair.split("_")

  def sellCurrency: Task[Currency] = for {
    s         <- dSide
    rawTicker <- Task.effect {
                   s match {
                     case Buy  => currencies(1)
                     case Sell => currencies.head
                   }
                 }
    qua       <- Task.effect(funds(rawTicker).toDouble)
    ticker    <- ZIO.fromEither(TickerSymbol.withNameEither(rawTicker))
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
    ticker    <- ZIO.fromEither(TickerSymbol.withNameEither(rawTicker))
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
      side      <- ZIO.fromEither(CCTraSide.withNameEither(res.side))
      createdAt <- CCTraCreatedAt(res.created_at)
      rate      <- res.dRate
    } yield CCTransaction(id, sellCur, buyCur, side, createdAt, rate)
  }
}
