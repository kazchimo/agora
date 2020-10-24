import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.2"
  lazy val sttp = "com.softwaremill.sttp.client3" %% "core" % "3.0.0-RC6"
  lazy val zio = "dev.zio" %% "zio" % "1.0.3"
  lazy val ziostreams = "dev.zio" %% "zio-streams" % "1.0.3"
}
