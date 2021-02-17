package domain.synatax

import _root_.enumeratum._
import lib.error.InternalDomainError
import zio.{IO, ZIO}

object enumeratum extends EnumeratumSyntax

trait EnumeratumSyntax {
  implicit final def domainEnumeratumSyntax[T <: EnumEntry, E <: Enum[T]](
    e: E
  ): EnumOps[T, E] = new EnumOps(e)
}

final class EnumOps[T <: EnumEntry, E <: Enum[T]](private val e: E)
    extends AnyVal {
  def withNameZio(entryName: String): IO[InternalDomainError, T] = ZIO
    .fromEither(e.withNameEither(entryName)).mapError(e =>
      InternalDomainError(
        s"Failed to create Enum model from String: String=$entryName",
        Some(e)
      )
    )
}
