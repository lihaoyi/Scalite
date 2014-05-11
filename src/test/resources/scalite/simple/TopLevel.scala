package scalite.simple

case object ObjectCase
    val w = 1

object ObjectLol
    val x = 100

trait MyTrait
    val y = 10

class TopLevel extends MyTrait
    def apply(): String =
        val z = 1
        import ObjectLol._
        import ObjectCase._
        "Hello World!" + (a + w + x + y + z)

    def a = 1
