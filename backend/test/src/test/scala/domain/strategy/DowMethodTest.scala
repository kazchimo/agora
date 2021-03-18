package domain.strategy

import domain.chart.OHLCBar
import zio.test.Assertion._
import zio.test._
import zio.{Chunk, Ref}

object DowMethodTest extends DefaultRunnableSpec {
  override def spec = suite("DowMethod")(
    testM("#updsateBars") {
      for {
        ref <- Ref.make(Chunk[Int]())
        a1  <- DowMethod.updateBars(ref, 3, 1)
        a2  <- DowMethod.updateBars(ref, 3, 2)
        a3  <- DowMethod.updateBars(ref, 3, 3)
        a4  <- DowMethod.updateBars(ref, 3, 4)
      } yield assert(a1)(equalTo(Chunk(1))) &&
        assert(a2)(equalTo(Chunk(1, 2))) &&
        assert(a3)(equalTo(Chunk(1, 2, 3))) &&
        assert(a4)(equalTo(Chunk(2, 3, 4)))
    },
    test("#shouldBuy") {
      assert(
        DowMethod.shouldBuy(
          Chunk(
            OHLCBar.unsafeApply(0, 0, 0, 0),
            OHLCBar.unsafeApply(1, 1, 1, 1),
            OHLCBar.unsafeApply(2, 2, 2, 2)
          )
        )
      )(isTrue) && assert(
        DowMethod.shouldBuy(
          Chunk(
            OHLCBar.unsafeApply(1, 1, 1, 1),
            OHLCBar.unsafeApply(2, 2, 2, 2),
            OHLCBar.unsafeApply(0, 0, 0, 0)
          )
        )
      )(isFalse) && assert(
        DowMethod.shouldBuy(
          Chunk(
            OHLCBar.unsafeApply(0, 0, 0, 0),
            OHLCBar.unsafeApply(0, 0, 0, 0),
            OHLCBar.unsafeApply(0, 0, 0, 0)
          )
        )
      )(isFalse)
    },
    test("#shouldSell") {
      assert(
        DowMethod.shouldSell(
          Chunk(
            OHLCBar.unsafeApply(0, 0, 0, 0),
            OHLCBar.unsafeApply(1, 1, 1, 1),
            OHLCBar.unsafeApply(2, 2, 2, 2)
          )
        )
      )(isFalse) && assert(
        DowMethod.shouldSell(
          Chunk(
            OHLCBar.unsafeApply(2, 2, 2, 2),
            OHLCBar.unsafeApply(1, 1, 1, 1),
            OHLCBar.unsafeApply(0, 0, 0, 0)
          )
        )
      )(isTrue) && assert(
        DowMethod.shouldSell(
          Chunk(
            OHLCBar.unsafeApply(0, 0, 0, 0),
            OHLCBar.unsafeApply(0, 0, 0, 0),
            OHLCBar.unsafeApply(0, 0, 0, 0)
          )
        )
      )(isFalse)
    }
  )
}
