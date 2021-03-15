package domain.exchange

import zio.macros.accessible
import zio.stream.UStream
import zio.{Has, RIO, ZIO}

package object liquid {
  type LiquidExchange = Has[LiquidExchange.Service]

  @accessible
  object LiquidExchange {
    import domain.AllEnv

    trait Service {
      def productsStream: ZIO[AllEnv, Throwable, UStream[LiquidProduct]]
    }
  }
}
