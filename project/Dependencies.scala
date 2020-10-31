import sbt._

object Dependencies {
  object Versions {
    val zio   = "1.0.3"
    val sttp  = "3.0.0-RC7"
    val circe = "0.13.0"

  }

  val circeDeps      = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-generic-extras",
    "io.circe" %% "circe-shapes",
    "io.circe" %% "circe-refined"
  ).map(_ % Versions.circe)

  lazy val scalaTest = "org.scalatest"                 %% "scalatest" % "3.2.2"
  lazy val sttp      = "com.softwaremill.sttp.client3" %% "core"      % Versions.sttp
  lazy val sttpCirce =
    "com.softwaremill.sttp.client3" %% "circe" % Versions.sttp
  lazy val sttpZio =
    "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % Versions.sttp
  lazy val zio             = "dev.zio"       %% "zio"              % Versions.zio
  lazy val ziostreams      = "dev.zio"       %% "zio-streams"      % Versions.zio
  lazy val zioMacros       = "dev.zio"       %% "zio-macros"       % Versions.zio
  lazy val zioCats         = "dev.zio"       %% "zio-interop-cats" % "2.2.0.1"
  lazy val codec           = "commons-codec"  % "commons-codec"    % "1.15"
  lazy val newtype         = "io.estatico"   %% "newtype"          % "0.4.4"
  lazy val refined         = "eu.timepit"    %% "refined"          % "0.9.17"
  lazy val chimney         = "io.scalaland"  %% "chimney"          % "0.6.0"
  lazy val cats            = "org.typelevel" %% "cats-core"        % "2.2.0"
  lazy val zioTest         = "dev.zio"       %% "zio-test"         % Versions.zio % Test
  lazy val zioTestSbt      = "dev.zio"       %% "zio-test-sbt"     % Versions.zio % Test
  lazy val zioTestMagnolia =
    "dev.zio" %% "zio-test-magnolia" % Versions.zio % Test
}
