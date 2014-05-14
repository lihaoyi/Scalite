package scalite.medium

trait T
  val a: Int
  def b: Int


abstract class C(_c: Int)
  val d: Int
  def e: Int
  var f: Int
  val c = _c


class Abstract extends C(1) with T
  val a = 10
  def b = 100
  val d = 1000
  def e = 10000
  var f = 100000
  def apply() =
    a + b + c + d + e + f
