package scalite

import utest._

import TestUtils._

object Tests extends TestSuite{
  val tests = TestSuite{
    'simple{
      'class-make("scalite.simple.Class", "Hello World!12")
      'assignments-make("scalite.simple.Assignments", 9)
      'modassignments-make("scalite.simple.ModAssignments", 9)
      'ifelsewhile-make("scalite.simple.IfElseWhile", 40)
      'ifelsewhilelite-make("scalite.simple.IfElseWhileLite", 36)
      'for-make("scalite.simple.For", 10100)
      'forlite-make("scalite.simple.ForLite", 10100)
      'match-make("scalite.simple.Match", "1")
      'multidef-make("scalite.simple.MultiDef", "Hello World!10")
      'toplevel-make("scalite.simple.TopLevel", "Hello World!113")
      'trycatch-make("scalite.simple.TryCatch", "lolnull")
    }
    'messy{
      'class-make("scalite.messy.Class", 12)
      'assignments-make("scalite.messy.Assignments", 9)
      'ifelsewhile-make("scalite.messy.IfElseWhile", 36)
      'for-make("scalite.messy.For", 100)
    }
    'medium{
      'modifiers-make("scalite.medium.Modifiers", 1111111)
      'abstract-make("scalite.medium.Abstract", 111111)
      'params-make("scalite.medium.Params", 11111)
      'matching-make("scalite.medium.Matching", 1)

    }
    'long{
      'for-make("scalite.long.For", "165 25")
      'while-make("scalite.long.While", 45)
      'if-make("scalite.long.If", 101)
    }
    'tutorial{
      'js-make("scalite.tutorial.Js", """false true null "LOL" 123 [true, false] {"hello": "WorldTrue"}""")
      'classes-make("scalite.tutorial.Classes", 11)
    }

  }
}


