package domain.exchange

import zio.{Has, Task}
import zio.macros.accessible
import zio.stream.UStream

package object liquid {
  type LiquidExchange = Has[LiquidExchange.Service]

  @accessible
  object LiquidExchange {
    trait Service {
      def productsStream: Task[UStream[LiquidProduct]]
    }
  }
}
