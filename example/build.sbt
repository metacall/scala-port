ThisBuild / scalaVersion := "2.13.4"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "metacall-scala-port-example",
    githubOwner := "example",
    githubRepository := "example-repo",
    githubTokenSource :=
      TokenSource.Environment("GITHUB_TOKEN") ||
        TokenSource.GitConfig("github.token"),
    resolvers += Resolver.githubPackages("metacall"),
    libraryDependencies ++= Seq(
      "io.metacall" %% "metacall" % "0.1.0",
      "org.scalatest" %% "scalatest" % "3.2.2" % Test
    )
  )
