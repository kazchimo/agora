package helpers.gen.std

import zio.random.Random
import zio.test.{Gen, Sized}

object StdGen {
  val positiveLongGen: Gen[Random, Long] =
    Gen.anyLong.map(a => if (a < 0) -a else a)

  val positiveDoubleGen: Gen[Random, Double] =
    Gen.anyDouble.map(a => if (a < 0) -a else a)

  val negativeLongGen: Gen[Random, Long] =
    Gen.anyLong.map(a => if (a > 0) -a else a)

  val negativeDoubleGen: Gen[Random, Double] =
    Gen.anyDouble.map(a => if (a > 0) -a else a)

  val nonEmptyStringGen: Gen[Random with Sized, String] =
    Gen.anyString.zipWith(Gen.anyChar)(_ + _)
}
