package helpers.gen.infra.exchange.coincheck.responses

import helpers.gen.std.StdGen.{positiveDoubleGen, positiveLongGen}
import infra.exchange.coincheck.responses.TransactionResponse
import zio.random.Random
import zio.test.{Gen, Sized}

object TransactionResponseGen {
  val transactionResponseGen: Gen[Random with Sized, TransactionResponse] =
    for {
      id          <- positiveLongGen
      orderId     <- Gen.anyLong
      createdAt   <- Gen.anyString
      jpyQua      <- positiveDoubleGen.map(_.toString)
      btcQua      <- positiveDoubleGen.map(_.toString)
      rate        <- positiveLongGen.map(_.toString)
      feeCurrency <- Gen.option(Gen.anyString)
      liquidity   <- Gen.anyString
      side        <- Gen.elements("buy", "sell")
    } yield TransactionResponse(
      id,
      orderId,
      createdAt,
      Map("jpy" -> jpyQua, "btc" -> btcQua),
      "btc_jpy",
      rate,
      feeCurrency,
      liquidity,
      side
    )
}
