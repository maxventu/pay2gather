package model

case class Description(label: String, date: String){
  override def toString: String = s"${label}, ${date}"
}
case class Record(users: Seq[User], amount: Double, cur: Currency){
  override def toString: String = s"${users.mkString(", ")} ${amount}${cur}"
}

case class PaymentMessage(description:Description, payments: Seq[Record], payer: User){
  override def toString: String =
    s"""
       |${payer} paid for ${description}
       |${payments.mkString("\n")}
       |""".stripMargin
}