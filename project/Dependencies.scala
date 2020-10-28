import sbt._

object Dependencies {
  object Versions {
    val zio   = "1.0.3"
    val sttp  = "3.0.0-RC7"
    val circe = "0.12.3"

  }

  val circeDeps      = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % Versions.circe)

  lazy val scalaTest = "org.scalatest"                 %% "scalatest" % "3.2.2"
  lazy val sttp      = "com.softwaremill.sttp.client3" %% "core"      % Versions.sttp
  lazy val sttpCirce =
    "com.softwaremill.sttp.client3" %% "circe" % Versions.sttp
  lazy val sttpZio =
    "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % Versions.sttp
  lazy val zio        = "dev.zio"       %% "zio"           % Versions.zio
  lazy val ziostreams = "dev.zio"       %% "zio-streams"   % Versions.zio
  lazy val zioMacros  = "dev.zio"       %% "zio-macros"    % Versions.zio
  lazy val codec      = "commons-codec"  % "commons-codec" % "1.15"
  lazy val newtype    = "io.estatico"   %% "newtype"       % "0.4.4"
  lazy val refined    = "eu.timepit"    %% "refined"       % "0.9.17"
  lazy val chimney    = "io.scalaland"  %% "chimney"       % "0.6.0"
  lazy val cats       = "org.typelevel" %% "cats-core"     % "2.2.0"
}
