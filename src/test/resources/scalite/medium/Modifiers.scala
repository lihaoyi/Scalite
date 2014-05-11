package scalite.medium

sealed trait T
  def a = 1

case class C(x: Int) extends T

final class Modifiers extends T
  lazy val b = 10
  private[this] var c = 100
  @volatile private var d = 1000
  private object Nested
    lazy val e = 10000
    private[medium] val f = 100000

  def apply(): Int =
    a + b + c + d + Nested.e + Nested.f + C(1000000).x


