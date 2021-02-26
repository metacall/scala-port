import Tests._

lazy val commonSettings = Seq(
  name := "metacall",
  scalaVersion := "2.13.4",
  version := "0.1.0-SNAPSHOT",
  organization := "com.metacall",
  organizationName := "metacall",
  scalacOptions ++= Seq(
    // Only for debugging purposes
    // "-Djava.compiler=NONE",
    "-feature",
    "-deprecation",
    "-Wunused:imports,patvars,privates,locals,explicits,implicits,params",
    "-Xlint",
    "-explaintypes",
    "-Wdead-code",
    "-Wextra-implicit",
    "-Wnumeric-widen",
    "-Wconf:cat=lint-byname-implicit:silent"
  ),
  scalacOptions in (Compile, console) := Seq.empty,
  libraryDependencies ++= Seq(
    "net.java.dev.jna" % "jna" % "5.6.0",
    "com.chuusai" %% "shapeless" % "2.3.3",
    "org.scalatest" %% "scalatest" % "3.2.2" % Test
  )
)

lazy val dockerTest = taskKey[Unit]("Run tests in metacall/core:dev")
dockerTest := {
  import scala.sys.process._
  import java.nio.file.Paths
  import sbt.Keys.streams

  val logger = streams.value.log
  logger.info(
    "NOTE: Run `./docker-compose.sh build` in the root of metacall/core first to get the latest metacall/core:dev image"
  )

  try s"docker run --rm -v ${Paths.get("").toAbsolutePath().toString()}:/tests metacall-scala-tests" !
  catch {
    case e: Throwable => {
      val msg =
        e.getMessage() + "\nTIP: Run `sbt build` if the image `metacall-scala-tests` doesn't exist"

      logger.err(msg)

      throw new Exception(msg)
    }
  }
}

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "metacall",
    fork in Test := true,
    testGrouping in Test := (testGrouping in Test).value.flatMap { group =>
      group.tests map (test => Group(test.name, Seq(test), SubProcess(ForkOptions())))
    },
    dockerfile in docker := new Dockerfile {
      from("metacall/core:dev")

      // Set up Scala and SBT using Coursier
      workDir("/")
      run("curl", "-fLo", "cs", "https://git.io/coursier-cli-linux")
      run("chmod", "+x", "cs")
      run("./cs", "setup", "--env", "--jvm", "11", "--apps", "sbt-launcher")
      env(
        "JAVA_HOME" -> "/root/.cache/coursier/jvm/adopt@1.11.0-9",
        "PATH" -> "/root/.cache/coursier/jvm/adopt@1.11.0-9/bin:/root/.local/share/coursier/bin:$PATH"
      )
      // To set up dependencies and SBT
      import java.nio.file.Paths
      copy(Paths.get("").toAbsolutePath().toFile(), new File("/_tests"))
      workDir("/_tests")
      run("sbt", "compile")

      // The SBT project root
      volume("/tests")
      workDir("/tests/")
      entryPoint("sbt", "test")
    },
    imageNames in docker := Seq(ImageName("metacall-scala-tests"))
  )
  .enablePlugins(DockerPlugin)
