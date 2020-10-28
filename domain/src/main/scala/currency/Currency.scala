package currency

import currency.Currency.CurQuantity
import domain.DomainError
import io.estatico.newtype.macros.newtype
import zio.{Task, ZIO}

sealed trait Currency {
  val quantity: CurQuantity
}

object Currency {
  @newtype case class CurQuantity(value: Double)

  def apply(quantity: Double, tickerSymbol: String): Task[Currency] =
    tickerSymbol match {
      case "btc" => ZIO.succeed(BitCoin(CurQuantity(quantity)))
      case "jpy" => ZIO.succeed(Yen(CurQuantity(quantity)))
      case _     => ZIO.fail(DomainError(s"invalid ticker symbol: $tickerSymbol"))
    }
}

final case class BitCoin(quantity: CurQuantity) extends Currency
final case class Yen(quantity: CurQuantity)     extends Currency
