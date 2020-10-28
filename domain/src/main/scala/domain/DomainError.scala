package domain

final case class DomainError(message: String) extends Exception
