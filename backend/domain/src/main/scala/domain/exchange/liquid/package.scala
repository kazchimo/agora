package domain.exchange

import enumeratum.EnumEntry.Lowercase
import enumeratum._
import zio.macros.accessible
import zio.stream.Stream
import zio.{Has, RIO}

package object liquid {
  type LiquidExchange = Has[LiquidExchange.Service]

  @accessible
  object LiquidExchange {
    import domain.AllEnv

    trait Service {
      def ordersStream(
        side: OrderSide
      ): RIO[AllEnv, Stream[Throwable, Seq[LiquidOrder]]]
      def productsStream: RIO[AllEnv, Stream[Throwable, LiquidProduct]]
      def executionStream: RIO[AllEnv, Stream[Throwable, LiquidExecution]]
    }

    sealed trait OrderSide extends Lowercase
    object OrderSide       extends Enum[OrderSide] {
      override def values: IndexedSeq[OrderSide] = findValues

      case object Buy  extends OrderSide
      case object Sell extends OrderSide
    }
  }
}
