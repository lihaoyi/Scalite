package scalite.long

class For
    def apply() =
        val all = for
            x <- 0 to 10
            y <- 0 to 10
            if x + y == 10
        yield
            val z = x * y
            z

        var i = 0

        for
            x <- 0 to 10
            y <- 0 to 10
            if x + y == 10
        do
            val z = x * y
            i += z

        i + " " + all.max
