package scalite

import utest._

import TestUtils._

class Tests extends TestSuite{
  val tests = TestSuite{
    'simple{
      'class-make("scalite.simple.Class")
      'assignments-make("scalite.simple.Assignments")
      'ifelsewhile-make("scalite.simple.IfElseWhile")
      'ifelsewhilelite-make("scalite.simple.IfElseWhileLite")
      'for-make("scalite.simple.For")
      'forlite-make("scalite.simple.ForLite")
      'match-make("scalite.simple.Match")
      'multidef-make("scalite.simple.MultiDef")
    }
    'messy{
      'class-make("scalite.messy.Class")
      'assignments-make("scalite.messy.Assignments")
      'ifelsewhile-make("scalite.messy.IfElseWhile")
      'for-make("scalite.messy.For")
    }
    'transformer-make("scalite.TransformerX")
  }
}


