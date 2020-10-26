import Dependencies._

ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val rootDeps       =
  Seq(scalaTest % Test, sttp, zio, ziostreams, codec, newtype, refined)

lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-Xlint",
    "-Xfatal-warnings",
    "-language:existentials",
    "-language:experimental.macros",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused",
    "-Ymacro-annotations"
  ),
  libraryDependencies ++= rootDeps
)

lazy val root = project
  .in(file("."))
  .aggregate(domain)
  .settings(name := "agora", commonSettings)

lazy val domain = project
  .in(file("domain"))
  .settings(commonSettings)
  .settings(moduleName := "domain", name := "domain")

lazy val infra = project
  .in(file("infra"))
  .settings(commonSettings)
  .settings(moduleName := "infra", name := "domain")
