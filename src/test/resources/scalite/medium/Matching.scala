package scalite.medium

class Matching
    def m(x: Int) =
        x match
            case x if x > 5 => 1
            case y if true  => 2
            case _ => 0


    def apply() = m(10)