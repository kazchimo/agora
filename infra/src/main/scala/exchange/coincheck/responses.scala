package exchange.coincheck

import currency.Currency
import exchange.Transaction
import exchange.Transaction.{TraCreatedAt, TraId, TraRate, TraSide}
import io.scalaland.chimney.Transformer
import zio.{Task, ZIO}

final case class TransactionsResponse(
  id: Long,
  orderId: Long,
  createdAt: String,
  funds: Map[String, String],
  pair: String,
  rate: String,
  feeCurrency: String,
  liquidity: String,
  side: String
)

object TransactionsResponse {
  implicit val toTransactionTransformer
    : Transformer[TransactionsResponse, Task[Transaction]] = res => {
    for {
      id         <- TraId(res.id)
      side       <- TraSide(res.side)
      currencies  = res.pair.split("_")
      sell       <- ZIO.effect(if (side.isBuy) currencies.head else currencies(1))
      buy        <- ZIO.effect(if (side.isBuy) currencies(1) else currencies.head)
      sellQua    <- ZIO.effect(res.funds(sell).toDouble)
      buyQua     <- ZIO.effect(res.funds(buy).toDouble)
      createdAt  <- TraCreatedAt(res.createdAt)
      doubleRate <- ZIO.effect(res.rate.toDouble)
      rate       <- TraRate(doubleRate)
      sellCur    <- Currency(sellQua, buy)
      buyCur     <- Currency(buyQua, sell)
    } yield Transaction(id, sellCur, buyCur, side, createdAt, rate)
  }
}
