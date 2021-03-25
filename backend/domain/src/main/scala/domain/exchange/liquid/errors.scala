package domain.exchange.liquid

import lib.error.{ClientErr, Error}

object errors {
  object NotEnoughBalance extends Error("Not enough balance", None, ClientErr)
}
