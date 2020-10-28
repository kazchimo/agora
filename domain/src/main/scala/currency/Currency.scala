package currency

import currency.Currency.CurQuantity
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.refineV
import io.estatico.newtype.macros.newtype
import zio.{IO, ZIO}

sealed trait Currency {
  val quantity: CurQuantity
}

object Currency {
  @newtype case class CurQuantity(value: Double Refined Positive)
  object CurQuantity {
    def apply(v: Double): IO[String, CurQuantity] =
      ZIO.fromEither(refineV[Positive](v).map(CurQuantity(_)))
  }

  def apply(quantity: Double, tickerSymbol: String): IO[String, Currency] =
    tickerSymbol match {
      case "btc" => CurQuantity(quantity).map(BitCoin)
      case "jpy" => CurQuantity(quantity).map(Yen)
      case _     => ZIO.fail(s"invalid ticker symbol: $quantity")
    }
}

final case class BitCoin(quantity: CurQuantity) extends Currency
final case class Yen(quantity: CurQuantity)     extends Currency
