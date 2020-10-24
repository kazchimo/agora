package example

case class  CoincheckApi(apiKey: String, apiSecret: String) {
  private def nonce = (System.currentTimeMillis() / 1000).toString
}

