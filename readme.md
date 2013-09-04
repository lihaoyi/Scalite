Scalite
=======

```scala
package scalite.simple                          package scalite.simple

class For                                       class For{
   def apply() =                                  def apply() = {
      var x = 0                                     var x = 0
      for i <- 0 until 10                           for(i <- 0 until 10){
         val j = i * 2                                val j = i * 2
         val k = j + 1                                val k = j + 1
         x += k                                       x += k
      "Hello World" + x                             }
                                                    "Hello World" + x
                                                  }
                                                }

```

**Scalite** is an experimental whitespace-delimited syntax for the scala programming language. This lets you delimit block scope using indentation rather than curly braces, reducing the amount of unnecessary curly braces within the source code. This is an important step in view of the great curly-brace shortage of 2007.

Check out the (small number of) working examples in the [tests](src/test/resources/scalite) folder, a full example in [TransformerX.scala](src/test/resources/scalite/TransformerX.scala), or read previous discussion on the [Google Group](https://groups.google.com/forum/#!topic/scala-language/yl9BRqlpjJ0).

Rather than being a hacky text-manipulator, Scalite is implemented as a modification to the Scala compiler that performs the transformation directly on the token-stream being produced by Scala's lexer. Scalite also has access to Scala's parser, which lets it recognize language constructs on an AST (and not just the token) level and hopefully provide a more robust implementation of the whitespace-delimited syntax.

Scalite is the culmination of about 20 hours of work, and isn't ready to be used for anything at all.


