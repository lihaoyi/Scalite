Scalite
=======

```scala
package scalite.simple

class Class
   def apply(): String =
      val x = 1
      "Hello World!" + x
```

**Scalite** is an experimental whitespace-delimited syntax for the scala programming language. This lets you delimit block scope using indentation rather than curly braces, reducing the amount of unnecessary curly braces within the source code.

Check out the (small number of) working examples in the [tests](src/test/resources/scalite) folder, a "realistic" example in [TransformerX.scala](src/test/resources/scalite/TransformerX.scala), or read previous discussion on the [Google Group](https://groups.google.com/forum/#!topic/scala-language/yl9BRqlpjJ0).

Scalite is the culmination of about 20 hours of work, and isn't ready to be used for anything at all.


