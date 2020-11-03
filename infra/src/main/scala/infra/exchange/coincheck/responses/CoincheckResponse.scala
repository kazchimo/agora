package infra.exchange.coincheck.responses

trait CoincheckResponse {
  val success: Boolean
}

trait SuccessCoincheckResponse extends CoincheckResponse {
  override val success: Boolean = true
}

trait FailedCoincheckResponse extends CoincheckResponse {
  override val success: Boolean = false
}
