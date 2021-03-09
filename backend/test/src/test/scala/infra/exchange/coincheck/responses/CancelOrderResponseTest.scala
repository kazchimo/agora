package infra.exchange.coincheck.responses

import io.circe.parser.decode
import zio.test.Assertion.{equalTo, isRight}
import zio.test._

object CancelOrderResponseTest extends DefaultRunnableSpec {
  override def spec = suite("CancelOrderResponse")(test("decoder") {
    val successJson = "{\n  \"success\": true,\n  \"id\": 12345\n}"
    val failJson    = "{\"success\": false, \"error\": \"error\"}"

    assert(decode[CancelOrderResponse](successJson))(
      isRight(equalTo(SuccessCancelOrderResponse(12345)))
    ) && assert(decode[CancelOrderResponse](failJson))(
      isRight(equalTo(FailedCancelOrderResponse("error")))
    )
  })
}
