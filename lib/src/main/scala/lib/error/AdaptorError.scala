package lib.error

sealed abstract class AdaptorError(
  override val msg: String,
  override val cause: Option[Throwable],
  override val code: ErrorCode
) extends Error(msg, cause, code)

final case class AdaptorClientError(
  override val msg: String,
  override val cause: Option[Throwable] = None
) extends AdaptorError(msg, cause, ClientErr)

final case class AdaptorInternalError(
  override val msg: String,
  override val cause: Option[Throwable] = None
) extends AdaptorError(msg, cause, InternalErr)
