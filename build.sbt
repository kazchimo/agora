import Dependencies._
import sbt.Keys.{envVars, fork}
import sbtwelcome.UsefulTask

ThisBuild / scalaVersion := "2.13.3"
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"

inThisBuild(
  Seq(
    addCompilerPlugin(scalafixSemanticdb),
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalafixScalaBinaryVersion := "2.13"
  )
)

lazy val rootDeps = Seq(
  codec,
  newtype,
  refined,
  chimney,
  cats,
  zioCats,
  zioLogging,
  zioMagic
) ++ circeDeps ++ zioDeps ++ sttpDeps ++ testDeps ++ monocleDeps ++ enumeratumDeps

lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-Xlint",
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
    Wart.DefaultArguments,
    Wart.ToString,
    Wart.TraversableOps,
    Wart.PublicInference,
    Wart.Product,
    Wart.Null,
    Wart.Equals,
    Wart.ToString,
    Wart.Recursion,
    Wart.Var
  ) ++ ContribWart.allBut(
    ContribWart.Apply,
    ContribWart.MissingOverride,
    ContribWart.SymbolicName,
    ContribWart.NoNeedForMonad,
    ContribWart.SomeApply
  ),
  addCompilerPlugin(
    ("org.typelevel" %% "kind-projector" % "0.11.3").cross(CrossVersion.full)
  ),
  Test / envFileName := "test.env",
  envVars in Test := (envFromFile in Test).value,
  fork in Test := true
)

logo :=
  """
    |    _
    |   / \   __ _  ___  _ __ __ _
    |  / _ \ / _` |/ _ \| '__/ _` |
    | / ___ \ (_| | (_) | | | (_| |
    |/_/   \_\__, |\___/|_|  \__,_|
    |        |___/
    |""".stripMargin

usefulTasks := Seq(
  UsefulTask("c", "compile", "Compile all modules"),
  UsefulTask("root", "project root", "Move to root project"),
  UsefulTask("api", "project apiServer", "Move to api project"),
  UsefulTask("domain", "project domain", "Move to domain project"),
  UsefulTask("infra", "project infra", "Move to infra project"),
  UsefulTask("t", "test", "Test"),
  UsefulTask("r", "reload", "Reload projects"),
  UsefulTask("fmt", "scalafmtAll; scalafixAll;", "Format code")
)

lazy val root = project
  .in(file("."))
  .aggregate(domain, infra, lib, test, application)
  .dependsOn(domain, infra, lib, test, application)
  .settings(name := "agora", commonSettings)

lazy val domain = project
  .in(file("domain"))
  .settings(commonSettings)
  .settings(moduleName := "domain", name := "domain")
  .dependsOn(lib)

lazy val application = project
  .in(file("application"))
  .settings(commonSettings)
  .settings(moduleName := "application", name := "application")
  .dependsOn(lib, domain)

lazy val infra = project
  .in(file("infra"))
  .settings(commonSettings)
  .settings(moduleName := "infra", name := "infra")
  .dependsOn(domain, lib)

lazy val lib = project
  .in(file("lib"))
  .settings(commonSettings)
  .settings(moduleName := "lib", name := "lib")

lazy val test = project
  .in(file("test"))
  .settings(commonSettings)
  .settings(moduleName := "test", name := "test")
  .dependsOn(lib, infra, domain, application)
