import sbt._

object Dependencies {
  object Versions {
    val zio        = "1.0.4-2"
    val sttp       = "3.1.7"
    val circe      = "0.13.0"
    val monocle    = "2.1.0"
    val enumeratum = "1.6.1"
  }

  lazy val monocleDeps = Seq(
    "com.github.julien-truffaut" %% "monocle-core"    % Versions.monocle,
    "com.github.julien-truffaut" %% "monocle-generic" % Versions.monocle,
    "com.github.julien-truffaut" %% "monocle-macro"   % Versions.monocle,
    "com.github.julien-truffaut" %% "monocle-state"   % Versions.monocle,
    "com.github.julien-truffaut" %% "monocle-refined" % Versions.monocle,
    "com.github.julien-truffaut" %% "monocle-unsafe"  % Versions.monocle,
    "com.github.julien-truffaut" %% "monocle-law"     % Versions.monocle % "test"
  )

  lazy val circeDeps      = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-generic-extras",
    "io.circe" %% "circe-shapes",
    "io.circe" %% "circe-refined"
  ).map(_ % Versions.circe)

  lazy val zioDeps        = Seq(
    "dev.zio" %% "zio",
    "dev.zio" %% "zio-streams",
    "dev.zio" %% "zio-macros"
  ).map(_ % Versions.zio)

  lazy val testDeps       = Seq(
    "dev.zio" %% "zio-test"          % Versions.zio,
    "dev.zio" %% "zio-test-sbt"      % Versions.zio,
    "dev.zio" %% "zio-test-magnolia" % Versions.zio
  ).map(_ % Test)

  lazy val sttpDeps       = Seq(
    "com.softwaremill.sttp.client3" %% "core",
    "com.softwaremill.sttp.client3" %% "circe",
    "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio"
  ).map(_ % Versions.sttp)

  lazy val enumeratumDeps =
    Seq("com.beachape" %% "enumeratum", "com.beachape" %% "enumeratum-circe")
      .map(_ % Versions.enumeratum)

  lazy val scalaTest      = "org.scalatest"        %% "scalatest"        % "3.2.2"
  lazy val zioCats        = "dev.zio"              %% "zio-interop-cats" % "2.3.1.0"
  lazy val zioLogging     = "dev.zio"              %% "zio-logging"      % "0.5.6"
  lazy val zioMagic       = "io.github.kitlangton" %% "zio-magic"        % "0.1.9"
  lazy val codec          = "commons-codec"         % "commons-codec"    % "1.15"
  lazy val newtype        = "io.estatico"          %% "newtype"          % "0.4.4"
  lazy val refined        = "eu.timepit"           %% "refined"          % "0.9.21"
  lazy val chimney        = "io.scalaland"         %% "chimney"          % "0.6.1"
  lazy val cats           = "org.typelevel"        %% "cats-core"        % "2.4.2"
}
