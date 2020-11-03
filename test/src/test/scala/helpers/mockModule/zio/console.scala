package helpers.mockModule.zio

import java.io.IOException

import zio.console.Console
import zio.{IO, UIO, ULayer, ZLayer}

object console {
  val empty: ULayer[Console] = ZLayer.succeed(new Console.Service {
    override def putStr(line: String): UIO[Unit] = UIO.succeed(())

    override def putStrErr(line: String): UIO[Unit] = UIO.succeed(())

    override def putStrLn(line: String): UIO[Unit] = UIO.succeed(())

    override def putStrLnErr(line: String): UIO[Unit] = UIO.succeed(())

    override def getStrLn: IO[IOException, String] = IO.succeed("")
  })
}
