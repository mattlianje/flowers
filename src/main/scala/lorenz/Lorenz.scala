package lorenz
import lorenz.Baudot.{bitwiseXOR, getBits, getCharacter}
import zio._

object Lorenz {

  case class Wheel(pins: List[Int], pos: Int) {
    def rotate: Wheel = copy(pos = (pos + 1) % pins.size)
    def bit: Int = pins(pos)
  }

  case class LorenzState(
      mu1: Wheel,
      mu2: Wheel,
      chi: List[Wheel],
      psi: List[Wheel],
      impulse: Int
  ) {
    def rotateMu1: LorenzState = copy(mu1 = mu1.rotate)
    def rotateMu2: LorenzState =
      if (mu1.bit == 1) copy(mu2 = mu2.rotate) else this
    def rotateChi: LorenzState = copy(chi = chi.map(_.rotate))
    def rotatePsi: LorenzState =
      if (mu1.bit != mu2.bit) copy(psi = psi.map(_.rotate)) else this
    def incrementImpulse: LorenzState = copy(impulse = impulse + 1)
  }

  /** At each key press ...
    * https://cs.stanford.edu/people/eroberts/courses/soco/projects/2008-09/colossus/lorenzmachine.html
    *      1. μ1 and χ wheels turn.
    *      2. μ2 turns iff μ1 has a value of 1.
    *      3. If μ1 ⊕ μ2 is 1 then all ψ wheels are turned, if not they don't.
    */

  object LorenzMachine {
    def createMachineState: UIO[Ref[LorenzState]] = {
      val chiWheels = List(41, 31, 29, 26, 23).map(pins =>
        Wheel((0 until pins).toList.map(i => i % 2), 0)
      )
      val psiWheels = List(43, 47, 51, 53, 59).map(pins =>
        Wheel((0 until pins).toList.map(i => i % 2), 0)
      )
      val muWheels = List(37, 61).map(pins =>
        Wheel((0 until pins).toList.map(i => i % 2), 0)
      )

      Ref.make(
        LorenzState(
          mu1 = muWheels.head,
          mu2 = muWheels(1),
          chi = chiWheels,
          psi = psiWheels,
          impulse = 0
        )
      )
    }

    def encipher(stateRef: Ref[LorenzState], input: String): UIO[String] = {
      input.toList.foldLeft(ZIO.succeed("")) { (prev, char) =>
        for {
          state <- stateRef.get
          inputBits = getBits(char.toString)
          chi = state.chi.map(_.bit).mkString
          psi =
            if (state.mu1.bit == 1 && state.mu2.bit == 1)
              state.psi.map(_.bit).mkString
            else "0" * 5
          xorBits = bitwiseXOR(chi, psi)
          encipheredBits = bitwiseXOR(inputBits.getOrElse("00000"), xorBits)
          encipheredChar = getCharacter(encipheredBits).getOrElse(" ")
          _ <- stateRef.update(_.rotateMu1.rotateChi.incrementImpulse)
          _ <- stateRef.update(_.rotateMu2)
          _ <- stateRef.update(_.rotatePsi)
          result <- prev.map(_ + encipheredChar)
        } yield result
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val runtime = Runtime.default

    val program = for {
      stateRef <- LorenzMachine.createMachineState
      encipheredMessage <- LorenzMachine.encipher(stateRef, "HELLO")
    } yield encipheredMessage

    Unsafe.unsafe {
      implicit u =>
        val result = runtime.unsafe.run(program)
        println(result)
    }
  }
}
