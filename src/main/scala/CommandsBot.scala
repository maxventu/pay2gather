import cats.effect.{Async, ContextShift, Timer}
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.bot4s.telegram.api.declarative.{Commands, RegexCommands}
import com.bot4s.telegram.cats.Polling
import com.bot4s.telegram.methods.{EditMessageText, ParseMode}
import com.bot4s.telegram.models.Message
import io.chrisdavenport.log4cats.Logger
import model.Currency.BYN
import model.GatheringException.{ParseError, PaymentError}
import model.GatheringException.ParseError.{EmptyMessage, UserNotAvailable}
import model._

import scala.collection.immutable
import scala.language.higherKinds
import scala.util.{Failure, Success, Try}

class CommandsBot[F[_]: Async: Timer : ContextShift: Logger](token: String) extends ExampleBot[F](token)
  with Polling[F]
  with Commands[F]
  with RegexCommands[F] {
  val replyArchive = new MessageArchive()
  val userIds = new UserArchive()

  def toTry[A](option: Option[A], failure: ParseError): Try[A] = option match {
    case Some(a) => Success(a)
    case None => Failure(failure)
  }

  def generateAnswerToPaymentMessage(msg: Message): Try[PaymentMessage] = {
    val entities = msg.entities
    entities.map(_.map(_.user.map(userIds.put)))
    (for {
      text <- toTry(msg.text, EmptyMessage)
      user <- toTry(msg.from, UserNotAvailable)
    } yield Parser.parsePaymentMessage(text, userIds.put(user), entities)).flatten
  }

  val helpMsg: String = Parser.escapeText(
    """Please provide a payment in following format:
      |/pay @user4 Meal at some place, 2020-04-20
      |@username1 @username2, User3 (20+30+40)*0.9BYN
      |@username1 @username2 20BYN
      |""".stripMargin)

  def replyToPaymentMsg(implicit msg: Message): F[Unit] = {
    val payment = generateAnswerToPaymentMessage(msg)
    val paymentText = payment match {
      case Success(p) => printPayment(p, userIds)
      case Failure(e: PaymentError) => s"${e}\n/help"
      case Failure(e) => s"Please fix your message by editing it\n${e}\n/help"
    }
    replyArchive.getReply match {
      case Some(reply) =>
        for {
          _ <- request(EditMessageText(
            chatId = Option(msg.chat.id),
            messageId = Option(reply.info.messageId),
            text = paymentText,
            parseMode = Option(ParseMode.Markdown)
          ))} yield replyArchive.put(reply.info.messageId, payment)
      case None => for {
        replyId <- replyWithAnswer(paymentText)
      } yield replyArchive.put(replyId, payment)
    }
  }

  def replyWithAnswer(msgText: String)(implicit message: Message): F[Int] =
    replyMd(
      msgText,
      replyToMessageId = Option(message.messageId),
      disableNotification = Some(true)
    ).map {
      replyMessage => replyMessage.messageId
    }

  onEditedMessage { implicit msg =>
    Logger[F].info(s"Text=${msg.text}, from=${msg.from}") >> {
      Parser.getCommand match {
        case Some("/pay") | Some("/pay4") => replyToPaymentMsg
        case _ => unit
      }
    }
  }

  onCommand("/pay") { implicit msg =>
    Logger[F].info(s"/pay request: ${msg}") >>
      replyToPaymentMsg
  }

  onCommand("/clean") { implicit msg =>
    Logger[F].info(s"/clean request: ${msg}") >> {
      replyArchive.msgToReplyId.clear()
      unit
    } >>
      replyWithAnswer("history cleaned").void
  }

  onCommand("/help") {
    implicit msg =>
    Logger[F].info(s"/help request: ${msg}") >>
      replyWithAnswer(helpMsg).void
  }

  onCommand("/stats") { implicit msg =>
    val defaultCurrency = BYN
    val successfulMessages: immutable.Seq[PaymentMessage] = replyArchive.msgToReplyId.values
      .filter(_.info.chatId == msg.chat.id)
      .flatMap(_.message.toOption).toList
    val payedMap = successfulMessages.flatMap { m =>
      for {
        payment <- m.payments
        user <- payment.users
        amountForOne = payment.amount * payment.cur.exchangeRateTo(defaultCurrency) / payment.users.length
      } yield (m.payer, user, amountForOne)
    } groupBy (a => (a._1, a._2)) mapValues (_.map(_._3).sum)
    val gathernessMap = payedMap.toSeq
      .flatMap { case ((from, to), amount) => Seq((from, amount), (to, -amount)) }
      .groupBy(_._1)
      .mapValues(_.map(_._2).sum)
    val spentMap = payedMap.toSeq
      .map { case ((_, to), amount) => (to, amount) }
      .groupBy(_._1)
      .mapValues(_.map(_._2).sum)
    if (successfulMessages.nonEmpty)
      replyWithAnswer(
        s"""Gatherness (${defaultCurrency}):
            |${gathernessMap.toList.map(a => f"${userIds.printUserWithId(a._1)}: ${a._2}%1.2f").mkString("\n")}
            |
            |Spent (${defaultCurrency}):
            |${spentMap.toList.map(a => f"${userIds.printUserWithId(a._1)}: ${a._2}%1.2f").mkString("\n")}
            |/help
            |""".stripMargin
    ).void
    else replyWithAnswer("There was nothing reported\n\n/help").void

  }

  def printPayment(paymentMessage: PaymentMessage, userIds: UserArchive): String =
    s"""
       |${userIds.printUserWithId(paymentMessage.payer)} paid for ${paymentMessage.description}
       |${paymentMessage.payments.map(printRecord(_,userIds)).mkString("\n")}
       |""".stripMargin

  def printRecord(record: Record, userIds: UserArchive) =
    s"${record.users.map(userIds.printUserWithId).mkString(", ")} ${record.amount}${record.cur}"
}


