package interface.gui
import zio.{ExitCode, URIO}

object Main extends zio.App {

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    Gui().run.exitCode
}
