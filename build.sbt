import Dependencies._

ThisBuild / scalaVersion := "2.13.3"

lazy val rootDeps =
  Seq(
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
    zioCats,
    zioTest,
    zioTestSbt,
    zioTestMagnolia
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
  testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  wartremoverErrors.in(Compile, compile) ++= Warts.allBut(
    Wart.Any,
    Wart.ImplicitConversion,
    Wart.ImplicitParameter,
    Wart.Nothing,
    Wart.Overloading
  )
)


addCommandAlias("root", ";project root")
addCommandAlias("api", ";project apiServer")
addCommandAlias("domain", ";project domain")
addCommandAlias("infra", ";project infra")
addCommandAlias("t", "test")

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
