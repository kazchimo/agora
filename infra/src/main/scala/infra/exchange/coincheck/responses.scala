package infra.exchange.coincheck

import domain.currency.{Currency, TickerSymbol}
import domain.exchange.coincheck.CCTransaction.{
  CCTraCreatedAt,
  CCTraId,
  CCTraRate,
  CCTraSide
}
import domain.exchange.coincheck.CCTransaction
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
)

object TransactionResponse {
  implicit val toTransactionTransformer
    : Transformer[TransactionResponse, Task[CCTransaction]] = res => {
    for {
      id         <- CCTraId(res.id)
      side       <- CCTraSide(res.side)
      currencies  = res.pair.split("_")
      sellTicker <- TickerSymbol(
                      if (side.isBuy) currencies.head else currencies(1)
                    )
      buyTicker  <- TickerSymbol(
                      if (side.isBuy) currencies(1) else currencies.head
                    )
      sellQua    <- ZIO.effect(res.funds(sellTicker.value).toDouble)
      buyQua     <- ZIO.effect(res.funds(buyTicker.value).toDouble)
      createdAt  <- CCTraCreatedAt(res.created_at)
      doubleRate <- ZIO.effect(res.rate.toDouble)
      rate       <- CCTraRate(doubleRate)
    } yield CCTransaction(
      id,
      Currency(sellTicker, sellQua),
      Currency(buyTicker, buyQua),
      side,
      createdAt,
      rate
    )
  }
}
