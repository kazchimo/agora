package domain.lib
import enumeratum._
import lib.error.ClientDomainError
import zio.{IO, ZIO}

trait ZEnum[A <: EnumEntry] extends Enum[A] {
  final def withNameZio(entryName: String): IO[ClientDomainError, A] = ZIO
    .fromEither(withNameEither(entryName)).mapError(e =>
      ClientDomainError(
        s"Failed to create Enum model from String: String=$entryName",
        Some(e)
      )
    )
}
