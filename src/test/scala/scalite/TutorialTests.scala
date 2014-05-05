package scalite
import utest._

object TutorialTests extends TestSuite{
  import TestUtils._
  val tests = TestSuite{
    'simple{
      'classes-make("scalite.Tutorial.Classes")
    }
  }
}


