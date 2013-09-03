package scalite

import org.scalatest._



class Tests extends FreeSpec{
  import TestUtils._

  "simple" - {
    "class" in make("scalite.simple.Class")
    "assignments" in make("scalite.simple.Assignments")
    "ifelsewhile" in make("scalite.simple.IfElseWhile")
    "for" in make("scalite.simple.For")
    "match" in make("scalite.simple.Match")
    "multidef" in make("scalite.simple.MultiDef")
  }
  "messy" - {
    "class" in make("scalite.messy.Class")
    "assignments" in make("scalite.messy.Assignments")
    "ifelsewhile" in make("scalite.messy.IfElseWhile")
    "for" in make("scalite.simple.For")
//    "match" in make("scalite.simple.Match")

  }

}


