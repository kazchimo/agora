package helpers.gen.std

import zio.random.Random
import zio.test.Gen

object StdGen {
  val positiveLongGen: Gen[Random, Long]     = Gen.anyLong.map(_ + 1)
  val positiveDoubleGen: Gen[Random, Double] = Gen.anyDouble.map(_ + 1)
}
