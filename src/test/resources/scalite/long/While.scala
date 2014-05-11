package scalite.long

class While
    def apply() =
        var i = 0
        var k = 0
        while
            println("Check!")
            var j = i + 1
            j < 10
        do
            println("Loop!")
            i += 1
            k += i

        k
