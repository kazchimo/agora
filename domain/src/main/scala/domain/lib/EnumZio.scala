package domain.lib
import enumeratum._
import lib.error.InternalDomainError
import zio.{IO, ZIO}

trait EnumZio[A <: EnumEntry] { self: Enum[A] =>
  final def withNameZio(entryName: String): IO[InternalDomainError, A] = ZIO
    .fromEither(self.withNameEither(entryName)).mapError(e =>
      InternalDomainError(
        s"Failed to create Enum model from String: String=$entryName",
        Some(e)
      )
    )
}
