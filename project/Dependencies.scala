import sbt._

object Dependencies {
  object Versions {
    lazy val zio  = "1.0.3"
    lazy val sttp = "3.0.0-RC7"
  }

  lazy val scalaTest = "org.scalatest"                 %% "scalatest" % "3.2.2"
  lazy val sttp      = "com.softwaremill.sttp.client3" %% "core"      % Versions.sttp
  lazy val sttpCirce =
    "com.softwaremill.sttp.client3" %% "circe" % Versions.sttp
  lazy val sttpZio =
    "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % Versions.sttp
  lazy val zio        = "dev.zio"      %% "zio"           % Versions.zio
  lazy val ziostreams = "dev.zio"      %% "zio-streams"   % Versions.zio
  lazy val zioMacros  = "dev.zio"      %% "zio-macros"    % Versions.zio
  lazy val codec      = "commons-codec" % "commons-codec" % "1.15"
  lazy val newtype    = "io.estatico"  %% "newtype"       % "0.4.4"
  lazy val refined    = "eu.timepit"   %% "refined"       % "0.9.17"
}
