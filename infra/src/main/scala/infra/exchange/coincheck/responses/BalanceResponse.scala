package infra.exchange.coincheck.responses

import cats.syntax.functor._
import io.circe.Decoder
import io.circe.generic.auto._

sealed trait BalanceResponse extends CoincheckResponse

object BalanceResponse {
  implicit val balanceResponseDecoder: Decoder[BalanceResponse] =
    List[Decoder[BalanceResponse]](
      Decoder[SuccessBalanceResponse].widen,
      Decoder[FailedBalanceResponse].widen
    ).reduce(_ or _)
}

final case class SuccessBalanceResponse(jpy: Double, btc: Double)
    extends BalanceResponse with SuccessCoincheckResponse

final case class FailedBalanceResponse(error: String)
    extends BalanceResponse with FailedCoincheckResponse
