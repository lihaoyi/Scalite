package scalite.messy

trait Cow
  def y = 10

class Thing(var x: Int)

class Class[T]
      extends Thing(
      1
   )
with
        Cow
   def apply() =
      1 + x + y


