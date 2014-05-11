package scalite.long

class If
    def apply() =
        func(0) + func(100)

    def func(i: Int) =
        if
            println("checking...")
            var j = i + 1
            j < 10
        do
            println("small")
            1
        else
            println("big")
            100

