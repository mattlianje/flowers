ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.1"

ThisBuild / organization := "io.github.mattlianje"
ThisBuild / organizationName := "mattlianje"
ThisBuild / organizationHomepage := Some(url("https://github.com/mattlianje/flowers"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/mattlianje/flowers"),
    "scm:git@github.com:mattlianje/flowers.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id    = "mattlianje",
    name  = "Matthieu Court",
    email = "matthieu.court@protonmail.com",
    url   = url("https://github.com/mattlianje")
  )
)

ThisBuild / description := "Cipher Machines for your Scala apps"
ThisBuild / licenses := List("GNU GPL v3" -> new URL("https://www.gnu.org/licenses/gpl-3.0.en.html"))
ThisBuild / homepage := Some(url("https://github.com/mattlianje/flowers"))

ThisBuild / pomIncludeRepository := { _ => false }

ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

ThisBuild / publishMavenStyle := true
ThisBuild / versionScheme := Some("early-semver")

lazy val root = (project in file("."))
  .settings(
    name := "flowers",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.12.0",
      "io.circe" %% "circe-core" % "0.14.10",
      "io.circe" %% "circe-generic" % "0.14.10",
      "io.circe" %% "circe-parser" % "0.14.10",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    )
  )

