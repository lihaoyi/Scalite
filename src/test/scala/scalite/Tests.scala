package scalite

import org.scalatest._



class Tests extends FreeSpec{
  import TestUtils._

  "trivial class" in {
    val first = make("scalite.simple.Class")
    println(first)
  }
  "assignments" in {
    val first = make("scalite.simple.Assignments")
    println(first)
  }
  "ifelsewhile" in {
    val first = make("scalite.simple.IfElseWhile")
    println(first)
  }
  "for" in {
    val first = make("scalite.simple.For")
    println(first)
  }
  "match" in {
    val first = make("scalite.simple.Match")
    println(first)
  }
  "multidef" in {
    val first = make("scalite.simple.MultiDef")
    println(first)
  }
  "ignored indent" in {
    val first = make("scalite.simple.IgnoredIndent")
    println(first)
  }
  "transformerX" in {
    val first = make("scalite.simple.TransformerX")
    println(first)
  }
}


