package model

object GatheringException {
  sealed abstract class ValidationError(message: String) extends Throwable(message) {
    override def toString: String = message
  }
  sealed abstract class ParseError(message: String) extends ValidationError(message)
  object ParseError {
    final case class DateFormatException(dateStr: String) extends ParseError(s"Not able to parse date: '${dateStr}'")
    final case class UnknownCurrency(currency: String) extends ParseError(s"Not familiar currency: '${currency}'. Possible: $$,BYN,€")
    final case class ExpressionFormatException(str: String) extends ParseError(s"Not able to parse expression: '${str}'")
    final case object EmptyMessage extends ParseError(s"Empty string")
    final case object UserNotAvailable extends ParseError(s"No user")
  }
}
