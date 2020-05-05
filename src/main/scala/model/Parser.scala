package model

import java.time.Year
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.temporal.{ChronoField, TemporalAccessor}

import cats.instances.list._
import cats.instances.try_._
import cats.syntax.traverse._
import com.bot4s.telegram.models.{Message, MessageEntity, MessageEntityType}
import model.GatheringException.ParseError.{DateFormatException, ExpressionFormatException}
import net.objecthunter.exp4j.ExpressionBuilder

import scala.annotation.tailrec
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object Parser {
  def getCommand()(implicit message: Message): Option[String] = message.text match {
    case Some(text) => Some(text.split("\\s+").head)
    case _ => None
  }
  def parseDouble(str: String) :Either[String,Double] = Try(str.toDouble) match {
    case Success(parsed) => Right(parsed)
    case Failure(_) => Left(str)
  }
  def calculateExpression(str: String): Try[Double] =
    Try(new ExpressionBuilder(str).build().evaluate()) match {
      case a: Success[Double] => a
      case _: Failure[Double] => Failure(ExpressionFormatException(str))
    }

  def parsePaymentMessage(text: String, sender: User, entities: Option[Seq[MessageEntity]]) : Try[PaymentMessage] = {
    val whoPaidFromTo: Option[(Int, Int)] = text.indexOf("\n") match {
      case i if i >= 0 => entities.map {
        _.filter(a => a.offset <= i && (a.`type` == MessageEntityType.Mention || a.`type` == MessageEntityType.TextMention))
          .map(a => (a.offset, a.offset + a.length))
          .minBy(_._1)
      }
      case _ => None
    }
    val (who, finalText) = whoPaidFromTo match {
      case Some((from,to)) => (User(text.slice(from,to)),text.slice(0,from) + text.slice(to,text.length))
      case None => (sender,text)
    }
    finalText.split("\n").filter(_ != "").toList match {
      case head :: tail =>
        for {
          description <- Parser.parseFirstLine(head)
          payments <- tail.traverse(Parser.parseLineRecord)
        } yield PaymentMessage(description, payments, who)
      case _ => Failure(ExpressionFormatException(text))
    }
  }

  // TODO: introduce more readable parsing
  def parseFirstLine(str: String): Try[Description] = {
    val commaParts = str.trim.split(",")
    val dateStr = commaParts.last
    val description = commaParts
      .dropRight(1)
      .mkString(",")
      .split(" ")
      .filter(_ != "")
      .drop(1)
      .mkString(" ")
    for {
      date <- Parser.normalizeDate(dateStr)
    } yield Description(description, date)
  }

  // TODO: introduce more readable parsing
  def parseLineRecord(str: String): Try[Record] = {
    val parts = str.trim.split("\\s+")
    val SumRegex = """(.*)(BYN|\$|€)"""r
    val users = parts.dropRight(1)
      .mkString(" ")
      .split(",")
      .flatMap(_.split("@"))
      .map(_.trim)
      .toList
      .filter(_ != "")
      .map(User)
    val lastPart = parts.last
    lastPart match {
      case SumRegex(expression, currencyStr) =>
        for {
          calculatedExpression <- calculateExpression(expression)
          currency <- Currency(currencyStr)
        } yield Record(users, calculatedExpression, currency)
      case _ => Failure(ExpressionFormatException(lastPart))
    }
  }

  def normalizeDate(dateStr: String): Try[String] = {
    val dateFormats = List(
      "dd/MM/uuuu",
      "dd MMMM uuuu",
      "dd MMM uuuu",
      "dd-MM-uuuu",
      "uuuu-MM-dd",
      "d.M.uuuu",
      "d.M.uu",
      "uuuu.MM.dd",
      "d.M",
    ).map(p => (p, new DateTimeFormatterBuilder().appendPattern(p).parseDefaulting(ChronoField.YEAR,Year.now.getValue).toFormatter))
    val iso8601DateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    val trimmedDate = dateStr.trim

    @tailrec
    def normalize(patterns: List[(String, DateTimeFormatter)]): Try[TemporalAccessor] = patterns match {
      case head::tail =>
        Try(head._2.parse(trimmedDate)) match {
          case Success(value) => Success(value)
          case Failure(_) => normalize(tail)
        }
      case _ => Failure(DateFormatException(dateStr))
    }
    normalize(dateFormats).map(iso8601DateFormatter.format)
  }
}
