package scalite.simple

class For
   def apply() =
      var x = 0
      for(i <- 0 until 10)
         val j = i * 2
         val k = j + 1
         x += k

      val list =
         for(i <- 0 to x) yield
            val j = i + 1
            i * j

      list.max
