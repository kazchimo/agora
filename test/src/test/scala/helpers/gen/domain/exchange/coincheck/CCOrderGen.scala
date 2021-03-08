package helpers.gen.domain.exchange.coincheck

import domain.exchange.coincheck.CCOrder.CCOrderId
import helpers.gen.std.StdGen.positiveLongGen
import zio.random.Random
import zio.test.Gen

object CCOrderGen {
  val ccOrderIdGen: Gen[Random, CCOrderId] =
    positiveLongGen.map(l => CCOrderId.unsafeFrom(l))
}
