ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "flowers",

    organization := "io.github.mattlianje",
    organizationName := "mattlianje",
    organizationHomepage := Some(url("https://github.com/mattlianje/flowers")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/mattlianje/flowers"),
        "scm:git@github.com:mattlianje/flowers.git"
      )
    ),
    developers := List(
      Developer(
        id = "mattlianje",
        name = "Matthieu Court",
        email = "matthieu.court@protonmail.com",
        url = url("https://github.com/mattlianje")
      )
    ),
    description := "Sz-40/42 Lorenz Machine for your Scala apps",
    licenses := List(
      "GNU GPL v3" -> new URL("https://www.gnu.org/licenses/gpl-3.0.en.html")
    ),
    homepage := Some(url("https://github.com/mattlianje/flowers")),
    pomIncludeRepository := { _ => false },
    publishTo := {
      val nexus = "https://s01.oss.sonatype.org/"
      if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
      else Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    publishMavenStyle := true
  )

val zio_version = "2.0.13"
libraryDependencies ++= Seq("dev.zio" %% "zio" % zio_version,
  "dev.zio" %% "zio-streams" % zio_version,
  "dev.zio" %% "zio-test" % zio_version % "test",
  "dev.zio" %% "zio-test-sbt" % zio_version % "test",
  "org.scalatest" %% "scalatest" % "3.2.15" % "test")
