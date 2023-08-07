import lorenz.Lorenz.LorenzMachine
import zio.{Runtime, Unsafe, ZIO}

class LorenzSpecs {
  def main(): Unit = {
    val runtime: Runtime[Any] = Runtime.default

    val program: ZIO[Any, RuntimeException, String] = for {
      lorenzMachine <- LorenzMachine.createMachine()
      encipheredMessage <- lorenzMachine match {
        case Left(err) => ZIO.fail(new RuntimeException(err))
        case Right(machine) => machine.encipher("hello")
      }
    } yield encipheredMessage

    Unsafe.unsafe { implicit u =>
      val result = runtime.unsafe.run(program)
      println(result)
    }
  }
}

