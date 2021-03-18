package domain.exchange

import zio.macros.accessible
import zio.stream.Stream
import zio.{Has, RIO}

package object liquid {
  type LiquidExchange = Has[LiquidExchange.Service]

  @accessible
  object LiquidExchange {
    import domain.AllEnv

    trait Service {
      def buyOrderStream: RIO[AllEnv, Stream[Throwable, LiquidOrder]]
      def productsStream: RIO[AllEnv, Stream[Throwable, LiquidProduct]]
      def executionStream: RIO[AllEnv, Stream[Throwable, LiquidExecution]]
    }
  }
}
