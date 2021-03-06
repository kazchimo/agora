package infra.exchange.coincheck.responses

import cats.syntax.functor._
import io.circe.Decoder
import io.circe.generic.auto._

sealed trait CancelOrderResponse extends CoincheckResponse

object CancelOrderResponse {
  implicit val cancelOrderResponseDecoder: Decoder[CancelOrderResponse] =
    List[Decoder[CancelOrderResponse]](
      Decoder[SuccessCancelOrderResponse].widen,
      Decoder[FailedCancelOrderResponse].widen
    ).reduceLeft(_ or _)
}

final case class SuccessCancelOrderResponse(id: Long)
    extends CancelOrderResponse with SuccessCoincheckResponse

final case class FailedCancelOrderResponse(error: String)
    extends CancelOrderResponse with FailedCoincheckResponse
