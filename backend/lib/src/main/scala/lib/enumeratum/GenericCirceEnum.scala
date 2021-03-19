package lib.enumeratum

import enumeratum.{CirceEnum, Enum, EnumEntry}
import io.circe.Encoder
import io.circe.syntax._

trait GenericCirceEnum[A <: EnumEntry] extends CirceEnum[A] { self: Enum[A] =>
  implicit def genericCirceEncoder[B <: A]: Encoder[B] =
    Encoder.instance(a => (a: A).asJson(circeEncoder))
}
