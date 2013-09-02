package scalite

class IgnoredIndent
  def apply(): String =
    if(true || false) do
      println(1)
      "lol"
    else
      "omg"


