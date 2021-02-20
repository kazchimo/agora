package infra.exchange.coincheck.responses

import zio.test.Assertion.{equalTo, hasField, hasSize, isRight, isSubtype}
import zio.test._
import io.circe.parser.decode

object OrdersResponseTest extends DefaultRunnableSpec {
  override def spec = suite("OrdersResponse")(test("decoder") {
    val successJson =
      "{\n  \"success\": true,\n  \"id\": 12345,\n  \"rate\": \"30010.0\",\n  \"amount\": \"1.3\",\n  \"order_type\": \"sell\",\n  \"stop_loss_rate\": null,\n  \"pair\": \"btc_jpy\",\n  \"created_at\": \"2015-01-10T05:55:38.000Z\"\n}"
    val failJson    = "{\"success\": false, \"error\": \"error\"}"

    assert(decode[OrdersResponse](successJson))(
      isRight(
        equalTo(
          SuccessOrdersResponse(
            12345,
            30010,
            1.3,
            "sell",
            None,
            "btc_jpy",
            "2015-01-10T05:55:38.000Z"
          )
        )
      )
    ) && assert(decode[OrdersResponse](failJson))(
      isRight(equalTo(FailedOrdersResponse("error")))
    )
  })
}
