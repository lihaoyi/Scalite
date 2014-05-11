package scalite.medium


class C(val x: Int, val y: Int = 10, val _z: Int = 1000)
  def z = _z


class Params extends C(1)
  def helper(x: Int = 100, y: Int = 0) = x + y
  def apply(): Int =
    x + y + helper(y=10000) + z


