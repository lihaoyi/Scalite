package scalite.simple

class IfElseWhileLite
   def apply() =
      var x = 0
      var y = 0

      while x < 10
         if x % 2 == 0
            x = x + 1
            y += x
         else
            x = x + 2
            y += x

      y

