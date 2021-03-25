package lib.zio

import zio.{Exit, Fiber, IO, UIO, ZRef}

final case class UpdatingRef[+EA, +EB, -A, +B, +FE, +FA](
  ref: ZRef[EA, EB, A, B],
  updateFiber: Fiber.Runtime[FE, FA]
) {
  def get: IO[EB, B] = ref.get

  def interruptUpdate: UIO[Exit[FE, FA]] = updateFiber.interrupt

  def map[C](f: B => C): UpdatingRef[EA, EB, A, C, FE, FA] =
    this.copy(ref.map(f))
}

object UpdatingRef {
  type UReadOnlyUpdatingRef[+A, +FE, +FA] =
    UpdatingRef[Nothing, Nothing, Nothing, A, FE, FA]
}
