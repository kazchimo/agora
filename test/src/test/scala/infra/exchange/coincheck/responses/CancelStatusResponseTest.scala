package infra.exchange.coincheck.responses

import zio.test.Assertion.{equalTo, isRight}
import zio.test._
import io.circe.parser.decode

object CancelStatusResponseTest extends DefaultRunnableSpec {
  override def spec = suite("CancelStatusResponse")(test("decoder") {
    val successJson =
      "{\n  \"success\": true,\n  \"id\": 12345,\n  \"cancel\": true,\n  \"created_at\": \"2020-07-29T17:09:33.000Z\"\n}"
    val failJson    = "{\"success\": false, \"error\": \"error\"}"

    assert(decode[CancelStatusResponse](successJson))(
      isRight(
        equalTo(
          SuccessCancelStatusResponse(12345, true, "2020-07-29T17:09:33.000Z")
        )
      )
    ) && assert(decode[CancelStatusResponse](failJson))(
      isRight(equalTo(FailedCancelStatusResponse("error")))
    )
  })

}
