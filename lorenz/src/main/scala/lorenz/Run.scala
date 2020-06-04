package lorenz
import cats.monocle.syntax._
import cats.instances.all._
import monocle.macros.Lenses


object Main extends App {
  println("test")

  val test = Baudot.XOR("10010", "00001", 0)

  // Example case classes ...
  @Lenses case class Address(street: String, city: String, zip: Int)
  @Lenses case class Person(name: String, age:Int, address: Address)

  val setStreet = Address.street := "13 Main St."
  val incrementAge = Person.age += 1

  val state = for {
    newStreetName <- Person.address %%= setStreet
    newAge <- incrementAge
  } yield (newStreetName, newAge)

  val person = Person("Alice", 30, Address("1 Main St", "San Francisco", 94123))
  val (changedPerson, (newStreet, newAge)) = state.run(person).value
  println(changedPerson.age)
}
