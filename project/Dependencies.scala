import sbt._

object Dependencies {

  object Versions {
    val fs2             = "2.3.0"
    val cats            = "2.1.1"
    val circe           = "0.13.0"
    val slf4j           = "1.7.30"
    val http4s          = "0.21.2"
    val newtype         = "0.4.3"
    val decline         = "1.0.0"
    val refined         = "0.9.13"
    val circeFs2        = "0.13.0"
    val scalaTest       = "3.1.1"
    val catsEffect      = "2.1.2"
    val enumeratumCirce = "1.5.23"
    val websocketClient = "0.2.0"
  }

  object Compile {
    val fs2             = "co.fs2" %% "fs2-core" % Versions.fs2
    val cats            = "org.typelevel" %% "cats-core" % Versions.cats
    val circe           = Seq("circe-core", "circe-parser", "circe-generic-extras").map("io.circe" %% _ % Versions.circe)
    val slf4j           = "org.slf4j" % "slf4j-nop" % Versions.slf4j
    val http4s          = Seq("http4s-circe", "http4s-blaze-server", "http4s-dsl").map("org.http4s" %% _ % Versions.http4s)
    val newtype         = "io.estatico" %% "newtype" % Versions.newtype
    val refined         = "eu.timepit" %% "refined" % Versions.refined
    val circeFs2        = "io.circe" %% "circe-fs2" % Versions.circeFs2
    val catsEffect      = "org.typelevel" %% "cats-effect" % Versions.catsEffect
    val enumeratumCirce = "com.beachape" %% "enumeratum-circe" % Versions.enumeratumCirce
    val websocketClient = "org.http4s" %% "http4s-jdk-http-client" % Versions.websocketClient
  }

  object Test {
    val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest % "test"
  }

  import Compile._
  import Test._

  lazy val dependencies = Seq(
    fs2,
    cats,
    slf4j,
    newtype,
    refined,
    circeFs2,
    scalaTest,
    catsEffect,
    enumeratumCirce,
    websocketClient,
  ) ++ circe ++ http4s
}
