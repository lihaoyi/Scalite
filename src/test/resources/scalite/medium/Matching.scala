package scalite.medium

class Matching
    def m(x: Int, y: Int) =
        x match
        case x if x > 5 =>
          y match
          case 1 => 0
          case _ => 1
        case x if x == y  => 2
        case _ => (x, y) match
        case (0, 1) => 3
        case (1, 2) =>
          x * y match
          case 2 => 4
          case _ => 5

    def apply() = m(9, 9) + " " + m(2, 2) + " " + m(1, 2)