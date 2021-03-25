package domain.exchange.liquid

import io.estatico.newtype.macros.newtype
import lib.refined.PositiveInt

object Pagination {
  @newtype case class Limit(value: PositiveInt)
}
