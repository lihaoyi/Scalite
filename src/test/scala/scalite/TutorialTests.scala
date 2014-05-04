package scalite
import utest._

class TutorialTests extends TestSuite{
  import TestUtils._
  val tests = TestSuite{
    'simple{
      'classes-make("scalite.Tutorial.Classes")
    }
  }
}


