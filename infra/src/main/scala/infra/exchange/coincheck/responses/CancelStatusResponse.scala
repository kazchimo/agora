package infra.exchange.coincheck.responses

import cats.syntax.functor._
import io.circe.Decoder
import io.circe.generic.auto._

sealed trait CancelStatusResponse extends CoincheckResponse

object CancelStatusResponse {
  implicit val cancelStatusResponseDecoder: Decoder[CancelStatusResponse] =
    List[Decoder[CancelStatusResponse]](
      Decoder[SuccessCancelStatusResponse].widen,
      Decoder[FailedCancelStatusResponse].widen
    ).reduceLeft(_ or _)
}

final case class SuccessCancelStatusResponse(
  id: Long,
  cancel: Boolean,
  created_at: String
) extends CancelStatusResponse with SuccessCoincheckResponse

final case class FailedCancelStatusResponse(error: String)
    extends CancelStatusResponse with FailedCoincheckResponse
