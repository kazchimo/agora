package domain.exchange.liquid

import lib.error.{ClientErr, Error, InternalErr}

object errors {
  sealed trait LiquidApiError extends Exception

  final case class BadRequest(body: String)
      extends Error(
        s"There was an error with the request. The body of the response will have more info: $body",
        None,
        ClientErr
      ) with LiquidApiError

  final case class UnprocessableEntity(body: String)
      extends Error(
        s"""
           |  There was an error with the request. The body of the response will have more info. Some possible reasons:
           |  - Missing params
           |  - The format of data is wrong
           |  - Not enough balance
           |  
           |  body: $body
           |  """.stripMargin,
        None,
        ClientErr
      ) with LiquidApiError

  final case class Unauthorized(body: String)
      extends Error(
        s"Unauthorized potentially for invalid tokens or nonce: $body",
        None,
        ClientErr
      ) with LiquidApiError

  object TooManyRequests
      extends Error(
        "This status indicates that the user has sent too many requests in a given amount of time",
        None,
        ClientErr
      ) with LiquidApiError

  final case class ServiceUnavailable(body: String)
      extends Error(
        s"""
          |  Many reasons, body will include details
          |  - An internal error on Authy.
          |  - Your application is accessing an API call you don't have access too.
          |  - API usage limit. If you reach API usage limits a 503 will be returned,
          |  please wait until you can do the call again.
          |  
          |  body: $body
          |  """.stripMargin,
        None,
        InternalErr
      ) with LiquidApiError
}
