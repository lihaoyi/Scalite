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
val x =
    val y = 1
    val z = 2
    y + z

var a =
    1 + 2 + 3

def apply() =
    x + a // 9
```

`y` and `z` are local variables only scoped to the definition of `x`, and not visible outside it. The same rule applies for `for` loops, `if`/`else`/`while`/`do`/`try` blocks. Here's some samples from the unit tests:

For loops
---------
```scala
var x = 0
for(i <- 0 until 10)
    val j = i * 2
    val k = j + 1
    x += k
x // 100
```
While/If/Else
-------------
```scala
var x = 0
var y = 0

while (x < 10)
    if (x % 2 == 0)
        x = x + 1
        y += x
    else
        x = x + 2
        y += x

y // 36
```
Top-level definitions
---------------------
```scala
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
        // Hello World!113

    val a = 1
```

Light syntax
============

In addition to indentation-scoped blocks, `for`/`if`/`while` blocks also support a paren-less syntax if the generators of the `for` or the conditional of the `if` or `while` fit on a single line:

```scala
var x = 0
for i <- 0 until 10
    val j = i * 2
    val k = j + 1
    x += k
x // 100

var x = 0
var y = 0

while x < 10
    if x % 2 == 0
        x = x + 1
        y += x
    else
        x = x + 2
        y += x

y // 36
```

Match blocks
============

Match blocks are similarly indentation delimited, but with a twist: since there isn't any ambiguity between more `case` clauses and statements outside the `match` block, Scalite does not require you to indent the `case` clauses, saving you one level of indentation:

```scala
val z = 1 match
case 1 =>
    println("One!")
    "1"
case 2 => "2"

z // "2"
```

The same applies to the `catch` block of a try-catch expression:

```scala
try
    println("Trying...")
    x.toString
catch
case n: NullPointerException =>
    println("Dammit")
    "null"
```

More?
=====

Want to see more? I have ported uPickle's pure-scala JSON parser to Scalite! Comparing the [original code](https://github.com/lihaoyi/upickle/blob/master/shared/main/scala/upickle/Js.scala) with the [Scalite version ](src/test/resources/scalite/tutorial/Js.scala) should give a good sense of what a messy, real world code base looks like in Scalite. You can also leaf through the [unit tests samples](src/test/resources/scalite/simple) for more examples of what Scalite code looks like.

Read previous discussion on the [Google Group](https://groups.google.com/forum/#!topic/scala-language/yl9BRqlpjJ0) if you want to know the background behind this.

Rewrite Rules
=============

Scalite implements approximately the following transform to convert the Scalite code to valid Scala:

- If the last tree T on a line is a class/trait/object-header,
def-header, var/val/lazyval, or control-flow construct
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


