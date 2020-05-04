import cats.effect.{Async, ContextShift}
import com.bot4s.telegram.cats.TelegramBot
import com.softwaremill.sttp.asynchttpclient.cats.AsyncHttpClientCatsBackend
import scala.language.higherKinds

abstract class ExampleBot[F[_]: Async : ContextShift](val token: String)
  extends TelegramBot(token, AsyncHttpClientCatsBackend())