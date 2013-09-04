package scalite

import org.scalatest._


class TutorialTests extends FreeSpec{
  import TestUtils._

  "simple" - {
    "class" in make("scalite.Tutorial.Classes")

  }

}


