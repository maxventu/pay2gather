import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Timer}
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import cats.syntax.applicativeError._
import cats.syntax.functor._

import scala.language.higherKinds

object Launcher extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    for {
      logger <- Slf4jLogger.create[IO]
      res <- helper[IO](args, logger)
    } yield res


  def helper[F[_]: ConcurrentEffect: ContextShift: Timer](args: List[String], log: Logger[F]): F[ExitCode] = {
    implicit val logger: Logger[F] = log
    args match {
      case List("pay2gather", token) =>
        (new CommandsBot[F](token))
          .startPolling
          .map(_ => ExitCode.Success)
      case List(name, _) =>
        (new Exception(s"Unknown bot $name")).raiseError
      case _ =>
        (new Exception("Usage:\nLauncher $botName $token")).raiseError
    }
  }

}