# Lorenz Sz-40/42
<img width="250" src="data/lorenz.jpg">

An implementation of the Lorenz Sz-40/42 cipher machine that was used by German high command during WW2. 
This library was mainly created to understand the abstractions offered by the ZIO framework. 

## Example
```scala
import zio.{Runtime, ZIO}
object MyApplication extends App {

  val myRuntime = Runtime.default

  val myTextToEncipher = "Hello, World!"

  val encipherProgram: ZIO[Any, Throwable, Unit] = for {
    lorenzMachine <- LorenzMachine.createMachine()
    encipheredMessage <- lorenzMachine match {
      case Left(err) =>
        ZIO.fail(new RuntimeException(err))
      case Right(machine) =>
        machine.encipher(myTextToEncipher)
    }
    _ <- ZIO.effectTotal(println(s"Plaintext: $myTextToEncipher"))
    _ <- ZIO.effectTotal(println(s"Enciphered Message: $encipheredMessage"))
  } yield ()

  myRuntime.unsafeRun(encipherProgram)
}
```