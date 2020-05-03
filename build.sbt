import Dependencies._

ThisBuild / scalaVersion     := "2.13.2"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.snac"
ThisBuild / organizationName := "snac"

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = (project in file("."))
  .settings(
    name := "discord",
    libraryDependencies ++= dependencies,
    scalacOptions += "-Ymacro-annotations"
  )

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)

fork           := true // Fork to separate process
connectInput   := true // Connects stdin to sbt during forked runs
outputStrategy := Some(StdoutOutput) // Get rid of output prefix
