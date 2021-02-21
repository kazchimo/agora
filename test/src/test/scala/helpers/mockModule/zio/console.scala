package helpers.mockModule.zio

import zio.console.Console
import zio.{IO, UIO, ULayer, ZLayer}

import java.io.IOException

object console {
  val devNull: ULayer[Console] = ZLayer.succeed(new Console.Service {
    override def putStr(line: String): UIO[Unit] = UIO.succeed(())

    override def putStrErr(line: String): UIO[Unit] = UIO.succeed(())

    override def putStrLn(line: String): UIO[Unit] = UIO.succeed(())

    override def putStrLnErr(line: String): UIO[Unit] = UIO.succeed(())

    override def getStrLn: IO[IOException, String] = IO.succeed("")
  })
}
