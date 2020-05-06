import Dependencies._

ThisBuild / scalaVersion := "2.13.2"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.snac"
ThisBuild / organizationName := "snac"

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val core = (project in file("."))
  .settings(
    name := "dissonance",
    scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, n)) if n >= 13 => Seq("-Ymacro-annotations")
      case _                       => Nil
    }),
    crossScalaVersions := List("2.13.2", "2.12.11"),
    libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v <= 12 => Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full))
      case _                       => Nil
    }) ++ dependencies
  )

addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.11.0" cross CrossVersion.full)

fork := true                         // Fork to separate process
connectInput := true                 // Connects stdin to sbt during forked runs
outputStrategy := Some(StdoutOutput) // Get rid of output prefix
