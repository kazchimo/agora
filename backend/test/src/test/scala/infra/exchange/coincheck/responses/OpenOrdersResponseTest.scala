package infra.exchange.coincheck.responses

import io.circe.parser.decode
import zio.test.Assertion._
import zio.test._

object OpenOrdersResponseTest extends DefaultRunnableSpec {
  override def spec = suite("OpenOrdersResponse")(test("decoder") {
    val successJson =
      "{\n  \"success\": true,\n  \"orders\": [\n    {\n      \"id\": 202835,\n      \"order_type\": \"buy\",\n      \"rate\": 26890,\n      \"pair\": \"btc_jpy\",\n      \"pending_amount\": \"0.5527\",\n      \"pending_market_buy_amount\": null,\n      \"stop_loss_rate\": null,\n      \"created_at\": \"2015-01-10T05:55:38.000Z\"\n    },\n    {\n      \"id\": 202836,\n      \"order_type\": \"sell\",\n      \"rate\": 26990,\n      \"pair\": \"btc_jpy\",\n      \"pending_amount\": \"0.77\",\n      \"pending_market_buy_amount\": null,\n      \"stop_loss_rate\": null,\n      \"created_at\": \"2015-01-10T05:55:38.000Z\"\n    },\n    {\n      \"id\": 38632107,\n      \"order_type\": \"buy\",\n      \"rate\": null,\n      \"pair\": \"btc_jpy\",\n      \"pending_amount\": null,\n      \"pending_market_buy_amount\": \"10000.0\",\n      \"stop_loss_rate\": \"50000.0\",\n      \"created_at\": \"2016-02-23T12:14:50.000Z\"\n    }\n  ]\n}"
    val failJson    = "{\"success\": false, \"error\": \"error\"}"

    assert(decode[OpenOrdersResponse](successJson))(
      isRight(
        isSubtype[SuccessOpenOrdersResponse](
          hasField("orders", _.orders, hasSize(equalTo(3)))
        )
      )
    ) && assert(decode[OpenOrdersResponse](failJson))(
      isRight(equalTo(FailedOpenOrdersResponse("error")))
    )
  })
}
