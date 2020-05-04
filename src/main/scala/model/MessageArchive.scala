package model

import com.bot4s.telegram.models.{ChatId, Message}

import scala.collection.parallel.mutable.ParMap
import scala.util.Try
final case class MessageInChat(messageId: Int, chatId: Long)
final case class PaymentMessageInChat(info: MessageInChat, message: Try[PaymentMessage])

class MessageArchive {
  // TODO: use atomic reference here
  var msgToReplyId: ParMap[MessageInChat, PaymentMessageInChat] = ParMap()

  def put(replyMessageId: Int, payment: Try[PaymentMessage])(implicit message: Message): Unit =
    msgToReplyId.put(
      MessageInChat(message.messageId, message.chat.id),
      PaymentMessageInChat(MessageInChat(replyMessageId, message.chat.id), payment)
    )

  def get(originalMessageId: Int)(implicit message: Message): Option[PaymentMessageInChat] =
    msgToReplyId.get(
      MessageInChat(originalMessageId, message.chat.id)
    )

  def removeResponse(implicit message: Message): ParMap[MessageInChat, PaymentMessageInChat] =
    msgToReplyId -= MessageInChat(message.messageId, message.chat.id)

  def getReply()(implicit message: Message): Option[PaymentMessageInChat] =
    msgToReplyId.get(
      MessageInChat(message.messageId, message.chat.id)
    )
}