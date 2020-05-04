import model.Parser

class CommandsBotTest extends org.scalatest.flatspec.AnyFlatSpec {
  it should "parse line and return Payment line" in {
    val inputMsg =
        """/pay4 Restaurant, 20.04.2019
        |@leontyev, @korzh 20+40$
        |@durov 39.99+49.99BYN
        |@rock 20€
        |""".stripMargin
  }
}
