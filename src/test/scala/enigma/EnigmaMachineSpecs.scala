package enigma

import machines.enigma.{EnigmaMachine, Plugboard, Reflector, Rotor}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EnigmaMachineSpecs extends AnyFlatSpec with Matchers {

  it should "encrypt and decrypt a message correctly" in {
    val rotor1 = Rotor("EKMFLGDQVZNTOWYHXUSPAIBRCJ", 'A', 'Q', 'A', "I")
    val rotor2 = Rotor("AJDKSIRUXBLHWTMCQGZNPYFVOE", 'A', 'E', 'A', "II")
    val rotor3 = Rotor("BDFHJLCPRTXVZNYEIWGAKMUSQO", 'A', 'V', 'A', "III")
    val reflector = Reflector("YRUHQSLDPXNGOKMIEBFZCWVJAT")
    val plugboard = Plugboard(Map.empty)
    val enigmaMachine =
      EnigmaMachine(List(rotor1, rotor2, rotor3), reflector, plugboard)

    val encryptedMessage = enigmaMachine.encrypt("HELLOWORLD")
    encryptedMessage shouldBe Right("EHPPKMKIPU")
    enigmaMachine.encrypt("EHPPKMKIPU") shouldBe Right("HELLOWORLD")
  }

}
