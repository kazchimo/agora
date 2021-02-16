package lib.error

sealed abstract class InfraError(
  override val msg: String,
  override val cause: Throwable,
  override val code: ErrorCode
) extends Error(msg, cause, code)

final case class InternalInfraError(
  override val msg: String,
  override val cause: Throwable
) extends InfraError(msg, cause, InternalErr)

final case class ClientInfraError(
  override val msg: String,
  override val cause: Throwable
) extends InfraError(msg, cause, ClientErr)
