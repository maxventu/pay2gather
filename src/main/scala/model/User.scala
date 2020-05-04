package model

import scala.collection.parallel.mutable.ParMap

// User is identified by userName or my Name and Surname
case class User(username: String){
  override def toString: String = username
}

class UserArchive{
  var userToId: ParMap[User, Int] = ParMap()

  def put(telegramUser: com.bot4s.telegram.models.User): User = {
    val user = User(telegramUser.username.getOrElse(telegramUser.firstName))
    userToId.put(user,telegramUser.id)
    user
  }
  def getId(user: User):Option[Int] =
    userToId.get(user)

  def printUserWithId(user: User):String =
    getId(user) match {
      case Some(realId) => s"[${user}](tg://user?id=${realId})"
      case None => s"${user}"
    }
}
