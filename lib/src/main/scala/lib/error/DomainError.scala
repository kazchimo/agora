package lib.error

sealed abstract class DomainError(
  override val msg: String,
  override val cause: Option[Throwable],
  override val code: ErrorCode
) extends Error(msg, cause, code)

final case class DomainClientError(
  override val msg: String,
  override val cause: Option[Throwable]
) extends DomainError(msg, cause, ClientErr)

final case class DomainInternalError(
  override val msg: String,
  override val cause: Option[Throwable]
) extends DomainError(msg, cause, InternalErr)
