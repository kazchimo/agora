import Dependencies._

ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val rootDeps =
  Seq(
    scalaTest % Test,
    sttp,
    zio,
    ziostreams,
    codec,
    newtype,
    refined,
    zioMacros,
    sttpZio,
    sttpCirce,
    chimney,
    cats,
    zioCats
  ) ++ circeDeps

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
  libraryDependencies ++= rootDeps,
  wartremoverErrors.in(Compile, compile) ++= Warts.allBut(
    Wart.Any,
    Wart.ImplicitConversion,
    Wart.ImplicitParameter,
    Wart.Nothing,
    Wart.Overloading
  )
)

lazy val root = project
  .in(file("."))
  .aggregate(domain, infra, apiServer, lib)
  .settings(name := "agora", commonSettings)

lazy val domain = project
  .in(file("domain"))
  .settings(commonSettings)
  .settings(moduleName := "domain", name := "domain")
  .dependsOn(lib)

lazy val infra = project
  .in(file("infra"))
  .settings(commonSettings)
  .settings(moduleName := "infra", name := "infra")
  .dependsOn(domain, lib)

lazy val apiServer = project
  .in(file("api-server"))
  .settings(commonSettings)
  .settings(moduleName := "apiServer", name := "apiServer")
  .dependsOn(domain, infra, lib)

lazy val lib = project
  .in(file("lib"))
  .settings(commonSettings)
  .settings(moduleName := "lib", name := "lib")
