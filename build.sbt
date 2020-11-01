import Dependencies._

ThisBuild / scalaVersion := "2.13.3"

lazy val rootDeps =
  Seq(
    codec,
    newtype,
    refined,
    chimney,
    cats,
    zioCats
  ) ++ circeDeps ++ zioDeps ++ sttpDeps ++ testDeps

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
    Wart.Overloading,
    Wart.DefaultArguments
  ),
  addCompilerPlugin(
    ("org.typelevel" %% "kind-projector" % "0.11.0").cross(CrossVersion.full)
  )
)

addCommandAlias("root", ";project root")
addCommandAlias("api", ";project apiServer")
addCommandAlias("domain", ";project domain")
addCommandAlias("infra", ";project infra")
addCommandAlias("t", "test")
addCommandAlias("c", "compile")

lazy val root = project
  .in(file("."))
  .aggregate(domain, infra, apiServer, lib, test)
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

lazy val test = project
  .in(file("test"))
  .settings(commonSettings)
  .settings(moduleName := "test", name := "test")
  .dependsOn(lib, apiServer, infra, domain)
