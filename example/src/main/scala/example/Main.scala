package example

import metacall._, instances._
import concurrent.ExecutionContext, ExecutionContext.Implicits.global
import concurrent.Await, concurrent.duration._
import java.nio.file.Paths

object Main extends App {
  Caller.start(ExecutionContext.global)

  val hello = for {
    _ <- Caller.loadFile(
      Runtime.Node,
      Paths
        .get("src/main/scala/example/my-functions.js")
        .toAbsolutePath()
        .toString()
    )
    result <- Caller.call("hello", "World!")
  } yield result

  println(Await.result(hello, 1.second))

  Caller.destroy()
}
