import sbt._

object Dependencies {

  object Versions {
    val fs2             = "2.4.2"
    val cats            = "2.1.1"
    val circe           = "0.13.0"
    val http4s          = "0.21.4"
    val newtype         = "0.4.4"
    val refined         = "0.9.14"
    val scalaTest       = "3.2.0"
    val catsEffect      = "2.1.3"
    val enumeratum      = "1.6.1"
    val websocketClient = "0.3.0"
  }

  object Compile {
    val fs2             = "co.fs2"        %% "fs2-core"               % Versions.fs2
    val cats            = "org.typelevel" %% "cats-core"              % Versions.cats
    val circe           = Seq("circe-core", "circe-parser", "circe-generic-extras").map("io.circe" %% _ % Versions.circe)
    val http4s          = "org.http4s"    %% "http4s-circe"           % Versions.http4s
    val newtype         = "io.estatico"   %% "newtype"                % Versions.newtype
    val refined         = "eu.timepit"    %% "refined"                % Versions.refined
    val catsEffect      = "org.typelevel" %% "cats-effect"            % Versions.catsEffect
    val enumeratum      = Seq("enumeratum", "enumeratum-circe").map("com.beachape" %% _ % Versions.enumeratum)
    val websocketClient = "org.http4s"    %% "http4s-jdk-http-client" % Versions.websocketClient
  }

  object Test {
    val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest % "test"
  }

  import Compile._
  import Test._

  lazy val dependencies = Seq(
    fs2,
    cats,
    http4s,
    newtype,
    refined,
    scalaTest,
    catsEffect,
    websocketClient
  ) ++ enumeratum ++ circe
}
