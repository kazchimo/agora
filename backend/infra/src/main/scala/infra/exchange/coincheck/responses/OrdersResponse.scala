package infra.exchange.coincheck.responses

import cats.syntax.functor._
import io.circe.Decoder
import io.circe.generic.auto._

sealed trait OrdersResponse extends CoincheckResponse

object OrdersResponse {
  implicit val ordersResponseDecoder: Decoder[OrdersResponse] =
    List[Decoder[OrdersResponse]](
      Decoder[SuccessOrdersResponse].widen,
      Decoder[FailedOrdersResponse].widen
    ).reduceLeft(_ or _)
}

final case class SuccessOrdersResponse(
  id: Long,
  rate: Double,
  amount: Double,
  order_type: String,
  stop_less_rate: Option[String],
  pair: String,
  created_at: String
) extends OrdersResponse with SuccessCoincheckResponse

final case class FailedOrdersResponse(error: String)
    extends OrdersResponse with FailedCoincheckResponse
