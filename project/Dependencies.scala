import sbt._

object Dependencies {

  object Versions {
    val fs2             = "3.0.1"
    val cats            = "2.6.0"
    val circe           = "0.13.0"
    val http4s          = "1.0.0-M21"
    val newtype         = "0.4.4"
    val refined         = "0.9.24"
    val scalaTest       = "3.2.8"
    val catsEffect      = "3.0.2"
    val enumeratum      = "1.6.1"
    val weaverTest      = "0.7.1"
    val websocketClient = "0.5.0-M4"
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
    val Test       = "test"
    val scalaTest  = "org.scalatest"       %% "scalatest"   % Versions.scalaTest  % Test
    val weaverTest = "com.disneystreaming" %% "weaver-cats" % Versions.weaverTest % Test
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
    weaverTest,
    websocketClient
  ) ++ enumeratum ++ circe
}
