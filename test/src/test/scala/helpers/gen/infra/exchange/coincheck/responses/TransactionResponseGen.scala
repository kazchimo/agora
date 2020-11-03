package helpers.gen.infra.exchange.coincheck.responses

import helpers.gen.std.StdGen.{
  nonEmptyStringGen,
  positiveDoubleGen,
  positiveLongGen
}
import infra.exchange.coincheck.responses.{
  FailedTransactionsResponse,
  SuccessTransactionsResponse,
  TransactionResponse,
  TransactionsResponse
}
import zio.random.Random
import zio.test.{Gen, Sized}

object TransactionResponseGen {
  val transactionResponseGen: Gen[Random with Sized, TransactionResponse] =
    for {
      id          <- positiveLongGen
      orderId     <- Gen.anyLong
      createdAt   <- nonEmptyStringGen
      jpyQua      <- positiveDoubleGen.map(_.toString)
      btcQua      <- positiveDoubleGen.map(_.toString)
      rate        <- positiveLongGen.map(_.toString)
      feeCurrency <- Gen.option(Gen.anyString)
      liquidity   <- nonEmptyStringGen
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

  val transactionsResponseGen: Gen[Random with Sized, TransactionsResponse] =
    Gen
      .listOf(transactionResponseGen)
      .map(t => SuccessTransactionsResponse(t))

  val failedTransactionsResponseGen
    : Gen[Random with Sized, TransactionsResponse] =
    Gen.anyString.map(FailedTransactionsResponse)
}
