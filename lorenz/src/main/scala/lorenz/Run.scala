package lorenz
import cats.monocle.syntax._
import cats.instances.all._
import monocle.macros.Lenses


object Main extends App {
  println("test")

  // Example case classes ...
  @Lenses case class Address(street: String, city: String, zip: Int)
  @Lenses case class Person(name: String, age:Int, address: Address)

}
