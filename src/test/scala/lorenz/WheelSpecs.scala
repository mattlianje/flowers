package lorenz

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class WheelSpecs extends AnyFlatSpec with Matchers {

  "A Wheel" should "create a wheel with specified pins and position" in {
    val wheel = Wheel(List(1, 0, 1, 0), 2)
    wheel.bit shouldBe 1
  }

  it should "rotate and wrap around correctly" in {
    val wheel = Wheel(List(1, 0, 1, 0), 3)
    wheel.rotate.pos shouldBe 0
  }

  it should "retrieve the current bit correctly" in {
    val wheel = Wheel(List(1, 0, 1, 0), 2)
    wheel.bit shouldBe 1
  }

  it should "calculate 1 deltas correctly" in {
    val wheel = Wheel(List(1, 0, 1, 0), 2)
    wheel.getDelta shouldBe 1
  }

  it should "calculate zero deltas correctly" in {
    val wheel = Wheel(List(1, 0, 1, 0, 0), 3)
    wheel.getDelta shouldBe 0
  }


  it should "throw an exception for invalid position" in {
    assertThrows[IllegalArgumentException] {
      Wheel(List(1, 0, 1, 0), 4)
    }
  }

  "The default wheel loader" should "load a wheel with alternating pins" in {
    val wheel = Wheel.loadDefaultWheel(4)
    wheel.pins shouldBe List(0, 1, 0, 1)
  }
}
