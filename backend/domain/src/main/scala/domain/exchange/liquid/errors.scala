package domain.exchange.liquid

import lib.error.{ClientErr, Error}

object errors {
  object NotEnoughBalance extends Error("Not enough balance", None, ClientErr)
  final case class Unauthorized(content: String)
      extends Error(
        s"Unauthorized potentially for invalid tokens or nonce: $content",
        None,
        ClientErr
      )
}
