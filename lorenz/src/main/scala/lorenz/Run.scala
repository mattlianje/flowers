package lorenz
import cats.instances.all._

object Main extends App {

  println("\nLorenz SZ-40/42 teleprinter cipher attachment as used by German high command in WW2 ...")
  println("\n\n Input message you would like to encipher ... ")

  val testMachine = Machines.getTestMachine()
  val user_input = scala.io.StdIn.readLine()
  val init = 0
  val res = ""

  runner(user_input, testMachine, init, res)

  def runner(str:String, machine:lorenzMachine, counter:Int, res:String): Unit = {
    if (res.length == str.length) {
      println("\n Cipher text: " + res)
    }
    else {
      val next_machine = machine.updateMachine
      val next_res = res + next_machine.encodeChar(str.charAt(counter).toString).toString
      val next_counter = counter + 1
      runner(str, next_machine, next_counter, next_res)
    }
  }
}
