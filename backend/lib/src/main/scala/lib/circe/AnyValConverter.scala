package lib.circe

import io.circe.{Decoder, Encoder}

object AnyValConverter extends AnyValConverter

trait AnyValConverter {
  import shapeless._

  implicit final def decodeAnyVal[T, U: Decoder](implicit
    ev: T <:< AnyVal,
    unwrapped: Unwrapped.Aux[T, U],
    decoder: Decoder[U]
  ): Decoder[T] = Decoder.instance[T] { cursor =>
    decoder(cursor).map(value => unwrapped.wrap(value))
  }

  implicit final def encodeAnyVal[T, U](implicit
    ev: T <:< AnyVal,
    unwrapped: Unwrapped.Aux[T, U],
    encoder: Encoder[U]
  ): Encoder[T] = Encoder.instance[T](value => encoder(unwrapped.unwrap(value)))

}
