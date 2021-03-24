package lib.syntax

import zio.ZIO

object _zio extends ZioSyntax

trait ZioSyntax {
  implicit final def libSyntaxBoolZio[R, E](
    a: ZIO[R, E, Boolean]
  ): BoolZioOps[R, E] = new BoolZioOps(a)
}

final class BoolZioOps[R, E](private val a: ZIO[R, E, Boolean]) extends AnyVal {
  def &&(that: ZIO[R, E, Boolean]): ZIO[R, E, Boolean] =
    ZIO.mapN(a, that)(_ && _)
}
