package domain

import domain.exchange.coincheck.CCTransaction.{Buy, Sell}
import zio.test._
import zio.test.Assertion._

object TransactionTest extends DefaultRunnableSpec {
  override def spec = suite("Transaction")(traSideTest)

  val traSideTest = suite("TraSide")(
    suite("isBuy")(
      test("Buy#isBuy returns true")(assert(Buy.isBuy)(equalTo(true))),
      test("Sell#isBuy returns false")(assert(Sell.isBuy)(equalTo(false)))
    ),
    suite("isSell")(
      test("Buy#isSell returns false")(assert(Buy.isSell)(equalTo(false))),
      test("Sell#isSell returns true")(assert(Sell.isSell)(equalTo(true)))
    )
  )
}
