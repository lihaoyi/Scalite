package scalite

import org.scalatest._



class Tests extends FreeSpec{
  import TestUtils._

  "trivial class" in {
    val first = make("scalite.TrivialClass")
    println(first)
  }
  /*"match" in {
    val first = make("scalite.Match")
    println(first)
  }
  "multidef" in {
    val first = make("scalite.MultiDef")
    println(first)
  }
  "ignored indent" in {
    val first = make("scalite.IgnoredIndent")
    println(first)
  }
  "transformerX" in {
    val first = make("scalite.TransformerX")
    println(first)
  }*/
}


