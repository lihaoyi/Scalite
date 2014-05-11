package scalite.medium

class IdentBlock
    def apply() =
        val xs = 0 until 10
        val ys = xs.map do
          x => x + 1

        val zs = xs.map do
          case 1 => 1
          case 2 => 2
          case x if x % 2 == 0 => x + 1
          case x if x % 2 != 0 => x - 1

        ys.sum + " " + zs.sum