Scalite
=======

```scala
package scalite.tutorial                                    package scalite.tutorial

class Point(xc: Int, yc: Int)                               class Point(xc: Int, yc: Int) {
    var x: Int = xc                                           var x: Int = xc
    var y: Int = yc                                           var y: Int = yc
    def move(dx: Int, dy: Int) =                              def move(dx: Int, dy: Int) = {
        x = x + dx                                              x = x + dx
        y = y + dy                                              y = y + dy
                                                              }
    override def toString() =                                 override def toString() = {
        "(" + x + ", " + y + ")"                                "(" + x + ", " + y + ")"
                                                              }
                                                            }
object Run                                                  object Run {
    def apply() =                                             def apply() = {
        val pt = new Point(1, 2)                                val pt = new Point(1, 2)
        println(pt)                                             println(pt)
        pt.move(10, 10)                                         pt.move(10, 10)
        pt.x                                                    pt.x
                                                              }
                                                            }
```

**Scalite** is an experimental whitespace-delimited syntax for the scala programming language. This lets you delimit block scope using indentation rather than curly braces, reducing the amount of unnecessary curly braces within the source code. This is an important step in view of the great curly-brace shortage of 2007.

Syntax
======

Scalite blocks are delimited by indentation rather than curly braces. Thus in the following code,

```scala
val x =                                                     val x = {
    val y = 1                                                 val y = 1
    val z = 2                                                 val z = 2
    y + z                                                     y + z
                                                            }
var a =                                                     var a = {
    1 + 2 + 3                                                 1 + 2 + 3
                                                            }
def apply() =                                               def apply() = {
    x + a                                                     x + a
    // 9                                                      // 9
                                                            }
```

`y` and `z` are local variables only scoped to the definition of `x`, and not visible outside it. The same rule applies for `for` loops, `if`/`else`/`while`/`do`/`try` blocks. Here's some samples from the unit tests:

For loops
---------
```scala
var x = 0                                                   var x = 0
for(i <- 0 until 10)                                        for(i <- 0 until 10) {
    val j = i * 2                                             val j = i * 2
    val k = j + 1                                             val k = j + 1
    x += k                                                    x += k
                                                            }
val list =                                                  val list = {
    for(i <- 0 to x) yield                                    for(i <- 0 to x) yield {
        val j = i + 1                                           val j = i + 1
        i * j                                                   i * j
                                                              }
                                                            }
list.max                                                    list.max
// 10100                                                    // 10100
```

Multi-line for- and for-yield blocks work too:

```scala
val all = for                                               val all = for {
    x <- 0 to 10                                              x <- 0 to 10
    y <- 0 to 10                                              y <- 0 to 10
    if x + y == 10                                            if x + y == 10
yield                                                       } yield {
    val z = x * y                                             val z = x * y
    z                                                         z
                                                            }
all.max                                                     all.max
// 25                                                       // 25
```

While/If/Else
-------------
```scala
var x = 0                                                   var x = 0
var y = 0                                                   var y = 0

while (x < 10)                                              while (x < 10) {
    if (x % 2 == 0)                                           if (x % 2 == 0) {
        x = x + 1                                               x = x + 1
        y += x                                                  y += x
    else                                                      } else {
        x = x + 2                                               x = x + 2
        y += x                                                  y += x
                                                              }
                                                            }
y                                                           y
// 36                                                       // 36
```
Top-level definitions
---------------------
```scala
case object ObjectCase                                      case object ObjectCase {
    val w = 1                                                   val w = 1
                                                            }
object ObjectLol                                            object ObjectLol {
    val x = 100                                                 val x = 100
                                                            }
trait MyTrait                                               trait MyTrait {
    val y = 10                                                  val y = 10
                                                            }
class TopLevel extends MyTrait                              class TopLevel extends MyTrait {
    def apply(): String =                                     def apply(): String = {
        val z = 1                                               val z = 1
        import ObjectLol._                                      import ObjectLol._
        import ObjectCase._                                     import ObjectCase._
        "Hello World!" + (a + w + x + y + z)                    "Hello World!" + (a + w + x + y + z)
        // Hello World!113                                      // Hello World!113
                                                              }
    val a = 1                                                 val a = 1
                                                            }
```
Match blocks
------------

Match blocks are similarly indentation delimited, but with a twist: since there isn't any ambiguity between more `case` clauses and statements outside the `match` block, Scalite does not require you to indent the `case` clauses, saving you one level of indentation:

```scala
val z = (1 + 1) match                                       val z = 1 match {
case 1 =>                                                     case 1 =>
    println("One!")                                             println("One!")
    "1"                                                         "1"
case 2 => "2"                                                 case 2 => "2"
                                                            }
z                                                           z
// "2"                                                      // "2"
```

The same applies to the `catch` block of a try-catch expression:

```scala
try                                                         try {
    println("Trying...")                                      println("Trying...")
    x.toString                                                x.toString
catch                                                       } catch {
case n: NullPointerException =>                               case n: NullPointerException =>
    println("Dammit")                                           println("Dammit")
    "null"                                                      "null"
                                                            }
```

Light syntax
------------

In addition to indentation-scoped blocks, `for`/`if`/`while` blocks also support a paren-less syntax if the generators of the `for` or the conditional of the `if` or `while` fit on a single line:

```scala
var x = 0                                                   var x = 0
for i <- 0 until 10                                         for (i <- 0 until 10) {
    val j = i * 2                                             val j = i * 2
    val k = j + 1                                             val k = j + 1
    x += k                                                    x += k
                                                            }
val list =                                                  val list = {
    for i <- 0 to x yield                                     for i <- 0 to x yield {
        val j = i + 1                                           val j = i + 1
        i * j                                                   i * j
                                                              }
                                                            }
list.max                                                    list.max
// 10100                                                    // 10100

var x = 0                                                   var x = 0
var y = 0                                                   var y = 0

while x < 10                                                while (x < 10) {
    if x % 2 == 0                                             if (x % 2 == 0) {
        x = x + 1                                               x = x + 1
        y += x                                                  y += x
    else                                                      } else {
        x = x + 2                                               x = x + 2
        y += x                                                  y += x
                                                              }
                                                            }
y                                                           y
// 36                                                       // 36
```

Custom Blocks
-------------

You can use whitespace-delimited blocks for your own functions too, and not just for built-in control flow constructs, using the `do` keyword:
```scala
val xs = 0 until 10                                         val xs = 0 until 10
val ys = xs.map do                                          val ys = xs.map{
  x => x + 1                                                  x => x + 1
                                                            }
ys.sum                                                      ys.sum
// 55                                                       // 55

val zs = xs.map do                                          val zs = xs.map{
case 1 => 1                                                   case 1 => 1
case 2 => 2                                                   case 2 => 2
case x if x % 2 == 0 => x + 1                                 case x if x % 2 == 0 => x + 1
case x if x % 2 != 0 => x - 1                                 case x if x % 2 != 0 => x - 1
                                                            }
zs.sum                                                      zs.sum
// 45                                                       // 45
```

The `do` at the end of the can be made optional with a slightly cleverer parser, but for now it is required.

You can also use the `do` with a function that takes an argument, like this:

```scala
val ws = xs.map do x =>                                     val ws = xs.map { x =>
    val x1 = x + 1                                            val x1 = x + 1
    x1 * x1                                                   x1 * x1
                                                            }
// 385                                                      // 385
```
Tall Headers
------------

Scalite supports spreading out the header of a for-loop over multiple lines:

```scala
val all = for                                               val all = for {
    x <- 0 to 10                                              x <- 0 to 10
    y <- 0 to 10                                              y <- 0 to 10
    if x + y == 10                                            if x + y == 10
yield                                                       } yield {
    val z = x * y                                             val z = x * y
    z                                                         z
                                                            }
all.max                                                     all.max
// 25                                                       // 25

var i = 0                                                   var i = 0
for                                                         for {
    x <- 0 to 10                                              x <- 0 to 10
    y <- 0 to 10                                              y <- 0 to 10
    if x + y == 10                                            if x + y == 10
do                                                          } {
    val z = x * y                                             val z = x * y
    i += z                                                    i += z
                                                            }
i                                                           i
// 165                                                      // 165
```

As well as the conditional of an if-statement:

```scala
if                                                          if ({
    println("checking...")                                    println("checking...")
    var j = i + 1                                             var j = i + 1
    j < 10                                                    j < 10
do                                                          }) {
    println("small")                                          println("small")
    1                                                         1
else                                                        } else {
    println("big")                                            println("big")
    100                                                       100
                                                            }
```

Or while loop:

```scala
var i = 0                                                   var i = 0
var k = 0                                                   var k = 0
while                                                       while({
    println("Check!")                                         println("Check!")
    var j = i + 1                                             var j = i + 1
    j < 10                                                    j < 10
do                                                          }){
    println("Loop!")                                          println("Loop!")
    i += 1                                                    i += 1
    k += i                                                    k += i
                                                            }
k                                                           k
// 45                                                       // 45
```

As you can see, the `do` keyword is used to indicate that the previous block has ended and a new block begins, in situations where in the default Scala syntax you only have a `}{` or `){` to separate these expressions. There should be no ambiguity with a do-while loop due to the fact that the do-while and while-do/for-do/if-dp always come together.


Redundant Do-While Loops
------------------------

Despite the fact that do-while loops still work, they are rendered redundant by the fact that the condition and body of the while loop are now symmetrical: Both sections of the loop can now easily hold an arbitrary block of statements, and any statements that you wish to execute *before* the condition is checked for the first time can simply be placed in the uppder block before the `do`.

Any do-while loop of the form

```scala
do {
  A
} while (B)
```

Can be equivalently rewritten as

```scala
while
    A
    B
()
```

With a slightly cleverer parser, the ugly `()` at the end of the loop can be removed, although for now it is required.

More?
=====

Want to see more? I have ported uPickle's pure-scala JSON parser to Scalite! Comparing the [original code](https://github.com/lihaoyi/upickle/blob/master/shared/main/scala/upickle/Js.scala) with the [Scalite version ](src/test/resources/scalite/tutorial/Js.scala) should give a good sense of what a messy, real world code base looks like in Scalite. You can also leaf through the [unit tests samples](src/test/resources/scalite/simple) for more examples of what Scalite code looks like.

Read previous discussion on the [Google Group](https://groups.google.com/forum/#!topic/scala-language/yl9BRqlpjJ0) if you want to know the background behind this.

Rewrite Rules
=============

Scalite implements approximately the following transform to convert the Scalite code to valid Scala:

- If the last tree T on a line is a class/trait/object-header,
def-header, var/val/lazyval, do, or control-flow construct
- And it is immediately followed by a `\n` with no `{`
- Then insert a `{` at the end of that line,
- And insert a `}` at the beginning of the first line whose indentation is less-than-or-equal to the indentation of the line of the *start* of the tree T and the first token of that line is not a `case`

Although there are a myriad of edge cases in this translation, this is the approximate algorithm that should explain the bulk of Scalite's behavior.

Notably, the fact that Scalite only special-cases lines which do not end in a `{` means that old-fashioned curly-brace Scala continues to function perfectly fine, and can be mixed together with Scalite code in the same source files and still compile without issue.

Implementation
==============
Rather than being a hacky text-manipulator, Scalite is implemented as a modification to the Scala compiler that performs the transformation directly on the token-stream being produced by Scala's lexer. Scalite also has access to Scala's parser, which lets it recognize language constructs on an AST (and not just the token) level and hopefully provide a more robust implementation of the whitespace-delimited syntax.

A more robust solution would be to fork the Scala compiler's recursive-descent parser, in order to properly insert the modifications in the correct places in the grammar. This would require forking the entire compiler code base, as the Scala compiler and parser have a circular type-level dependency on each other, and the parser cannot be easily swapped out. Although possible, this approach was more work than I was willing to put in.

Scalite is implemented as a custom `Global` rather than as a compiler plugin, because the Scala compiler architecture does not allow compiler plugins to replace the lexing-and-parsing phase of the compilation pipeline. This makes it difficult to bundle up and re-use in other projects. Nevertheless, there is a moderately large suite of unit tests which uses this custom `Global` to programmatically compile chunks of code and executes them to ensure they behave as expected.

Scalite is the culmination of about 30 hours of work, and isn't ready to be used for anything at all. The semantics are full of bugs, and the implementation is a rats nest of complexity, but *it works*, and hopefully will inspire or convince someone somewhere that a whitespace-based syntax is something worth trying out.


