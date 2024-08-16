import mill._
import mill.scalalib._

object flowers extends ScalaModule {

  def scalaVersion = "2.13.14"

  def ivyDeps = Agg(
    ivy"org.typelevel::cats-core::2.12.0"
  )

  object test extends ScalaTests with TestModule.Munit with TestModule.ScalaTest {
    def ivyDeps = Agg(
      ivy"org.scalameta::munit::1.0.0",
      ivy"org.scalatest::scalatest::3.2.19",
      ivy"org.typelevel::cats-core::2.12.0",
    )
  }
}

