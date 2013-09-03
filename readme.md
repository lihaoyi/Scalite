Scalite
=======

```scala
package scalite.simple

class For
   def apply() =
      var x = 0
      for(i <- 0 until 10)
         val j = i * 2
         val k = j + 1
         x += k
      "Hello World" + x

```

**Scalite** is an experimental whitespace-delimited syntax for the scala programming language. This lets you delimit block scope using indentation rather than curly braces, reducing the amount of unnecessary curly braces within the source code.

Check out the (small number of) working examples in the [tests](src/test/resources/scalite) folder, a full example in [TransformerX.scala](src/test/resources/scalite/TransformerX.scala), or read previous discussion on the [Google Group](https://groups.google.com/forum/#!topic/scala-language/yl9BRqlpjJ0).

Rather than being a hacky text-manipulator, Scalite is implemented as a modification to the Scala compiler that performs the transformation directly on the token-stream being produced by Scala's lexer. Scala also has access to Scala's parser, which lets it recognize language constructs on an AST (and not just the token) level and hopefully provide a more robust implementation of the whitespace-delimited syntax.

Scalite is the culmination of about 20 hours of work, and isn't ready to be used for anything at all.


