package lib.error

sealed abstract class ApplicationError(
  override val msg: String,
  override val cause: Option[Throwable],
  override val code: ErrorCode
) extends Error(msg, cause, code)

final case class ClientApplicationError(
  override val msg: String,
  override val cause: Option[Throwable] = None
) extends ApplicationError(msg, cause, ClientErr)

final case class InternalApplicationError(
  override val msg: String,
  override val cause: Option[Throwable] = None
) extends ApplicationError(msg, cause, InternalErr)
