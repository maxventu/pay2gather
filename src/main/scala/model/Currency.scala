package model

import model.GatheringException.ParseError.UnknownCurrency

import scala.util.{Failure, Success, Try}

abstract class Currency(sign: String){
  override def toString: String = sign
  def exchangeRateTo(other: Currency): Double
}
object Currency{
  final case object BYN    extends Currency(sign = "BYN") {
    override def exchangeRateTo(other: Currency): Double = other match {
      case BYN    => 1.0
      case Dollar => 1 / Dollar.exchangeRateTo(BYN)
      case Euro   => 1 / Euro.exchangeRateTo(BYN)
    }
  }
  final case object Dollar extends Currency(sign = "$"){
    override def exchangeRateTo(other: Currency): Double = other match {
      case BYN => 2.4143
      case Dollar => 1.0
      case Euro => Dollar.exchangeRateTo(BYN) / Euro.exchangeRateTo(BYN)
    }
  }
  final case object Euro   extends Currency(sign = "€") {
    override def exchangeRateTo(other: Currency): Double = other match {
      case BYN => 2.6261
      case Dollar => 1 / Dollar.exchangeRateTo(Euro)
      case Euro => 1.0
    }
  }

  def apply(sign: String): Try[Currency] = sign match {
    case "BYN" => Success(BYN)
    case "$" => Success(Dollar)
    case "€" => Success(Euro)
    case _ => Failure(UnknownCurrency(sign))
  }


}
