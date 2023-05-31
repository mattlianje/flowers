ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "flowers-2"
  )

val zio_version = "2.0.13"
libraryDependencies ++= Seq("dev.zio" %% "zio" % zio_version,
  "dev.zio" %% "zio-streams" % zio_version,
  "dev.zio" %% "zio-test" % zio_version % "test",
  "dev.zio" %% "zio-test-sbt" % zio_version % "test",
  "org.scalatest" %% "scalatest" % "3.2.15" % "test")