package interface.gui

import zio._
import zio.console.{Console, getStrLn, putStrLn}

import java.io.IOException

final case class Gui() {
  def run: ZIO[Console, IOException, Unit] = for {
    s <- getStrLn
    _ <- putStrLn(s)
  } yield ()
}
