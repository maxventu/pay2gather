package model

import model.Currency.{BYN, Dollar}
import org.scalatest.TryValues

import scala.util.{Failure, Success}

class ParserTest extends org.scalatest.flatspec.AnyFlatSpec {
  it should "parse different formats of dates same way" in {
    val inputs = List(
      "09/04/2020",
      "09 April 2020",
      "09 Apr 2020    ",
      "09-04-2020",
      "    2020-04-09   ",
      "09.04.2020",
      "09.04.20",
      "09.04.2020",
      "9.4.2020",
      "9.4.20",
      "2020.04.09",
      "09.04",
      "   09.4",
      "9.4",
      "9.04",
    )
    inputs.map(d =>
      assertResult
      (Success("2020-04-09"))
      (Parser.normalizeDate(d))
    )
  }
  it should "fail, if format of date wasn't recognized" in {
    val inputs = List(
      "09/04_2020",
      "09 Aprilt 2020",
      "39 Apr 2020    ",
      "09+04-2020",
      " dd   2020-04-09   ",
      "09.04,2020",
      "09.04.20--",
      "00.04.2020",
      "9.0.2020",
      "9.4.-00",
      "2a20.04.09",
      "0904",
      "   dd.mm",
      "9",
      ".4",
      "9.",
    )
    inputs.map(d =>
      assert(Parser.normalizeDate(d).isFailure, s"date str was: $d")
    )
  }

  it should "parse first line" in {
    val inputs = List(
      "/pay4 Dinner, in Brioche, 09.04.2020",
      "/pay4 Dinner, in Brioche, 9.4",
    )
    inputs.map(str =>
      assertResult
      (Success(Description("Dinner, in Brioche", "2020-04-09")))
      (Parser.parseFirstLine(str))

    )
  }

  it should "parse for whom and which sum was paid" in {
    val mapping: Map[String, Record] = Map(
      "@user1@user2 @user3 20$" -> Record(Seq(User("user1"),User("user2"),User("user3")), 20, Dollar),
      "@u1,User_2,User3@user4, User_5 20-(110-10)*0.7BYN" -> Record(Seq(User("u1"),User("User_2"),User("User3"),User("user4"),User("User_5")), -50, BYN),
    )
    for (m <- mapping) {
      assertResult(Success(m._2))(Parser.parseLineRecord(m._1))
    }
  }
}
