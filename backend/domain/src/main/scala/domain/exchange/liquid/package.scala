package domain.exchange

import zio.macros.accessible
import zio.stream.Stream
import zio.{Has, ZIO}

package object liquid {
  type LiquidExchange = Has[LiquidExchange.Service]

  @accessible
  object LiquidExchange {
    import domain.AllEnv

    trait Service {
      def productsStream
        : ZIO[AllEnv, Throwable, Stream[Throwable, LiquidProduct]]
    }
  }
}
