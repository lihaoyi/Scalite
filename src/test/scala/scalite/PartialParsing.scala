package scalite

import org.scalatest._
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.Settings
import scala.reflect.internal.util.{BatchSourceFile, SourceFile}
import scala.reflect.io.VirtualFile
import scala.tools.nsc.ast.parser.Tokens


class PartialParsing extends FreeSpec{
  import TestUtils._

  "parsing" in {
    import Tokens._

    assert(parsePartial(_.parseAll, "class X").map(_.token) === Seq(CLASS, IDENTIFIER))
    assert(parsePartial(_.parseAll, "class X[T]{}").map(_.token) === Seq(
      CLASS, IDENTIFIER, LBRACKET, IDENTIFIER, RBRACKET, LBRACE, RBRACE
    ))
    assert(parsePartial(_.parseAll, "if if } } else class object 1 2 3").map(_.token) === Seq(
      IF, IF, RBRACE, RBRACE, ELSE, CLASS, OBJECT, INTLIT, INTLIT, INTLIT
    ))
  }

//  "class trait object" in {
//    def check(s: String, n: Int) = assert(parsePartial(_.declHeader, s) === n)
//
//    check("class X", 2)
//    check("class X[T]", 5)
//    check("class X{ val x = 1 }", 2)
//    check("class X extends Y{ val x = 1 }", 4)
//    check("class X[T](val x: Int = 10) extends Y(x){ val x = 1 }", 18)
//
//    check("class \n\n\nX    [\n\nT\n](val x:\n\n Int =\n 10\n\n) extends Y(x\n\n){ val x = 1 }", 18)
//
//    check("object XYZ{ val x = 1}", 2)
//
//    check("trait XYZ[T] extends Cow { self =>  val x = 1}", 7)
//
//    check("case object X", 2)
//    check("case class X()", 4)
//  }

  "defs" in {
    def check(s: String, n: Int) = assert(parsePartial(_.defHeader, s) === n)
    check("def x = 10", 3)
    check("def x[T](a: Int, b: String): T = 10", 17)
    check("def\n x[\n\nT\n](a\n\n: Int\n, \nb: String)\n\n: T \n= 10", 17)
  }

  "val var" in {
    def check(s: String, n: Int) = assert(parsePartial(_.valVarHeader, s) === n)
    check("val x = 10", 3)
    check("val (x, y) = (1, 2)", 7)
    check("val x, y = 3", 5)
    check("val x, (y, z)\n = \n{3}", 9)
  }

  "if else" in {
    def check(s: String, n: Int) = assert(parsePartial(_.ifWhileHeader, s) === n)
    check("if (true) 1 else 2", 4)
    check("if \n(\ntrue\n\n           )\n\n\n 1 else 2", 4)
    check("while (true == false) 1", 6)
    check("while \n\n\n(\n\ntrue\n          == false) 1", 6)
    check("if(if(true) 1 else 2) 1 else 2", 10)
  }

  "for loops" in {
    def check(s: String, n: Int) = assert(parsePartial(_.forHeader, s) === n)
    check("for (x <- xs) 1 ", 6)
    check("for {x <- xs} 1 ", 6)
    check("for (x <- xs) yield 1 ", 7)
    check("for (x <- for{y <- z} 1) yield 1 ", 13)
    check("for \n\n{x <- xs\ny <- ys\n\n} yield 1 ", 11)
  }

  "test" in {
    def check(s: String, n: Int) = assert(parsePartial(_.declHeader, s) == n)
  }


  def parsePartial[T](f: Transformer#PartialParser => T, s: String) = {
    val settings = {
      val s =  new Settings
      //s.Xprint.value = List("all")
      val classPath = getFilePaths("/Runtimes/scala-2.11.0-M4/lib") :+ "target/scala-2.10/classes"

      classPath.map(new java.io.File(_).getAbsolutePath).foreach{ f =>
        s.classpath.append(f)
        s.bootclasspath.append(f)
      }
      s
    }

    val compiler = new Globalite(settings, new ConsoleReporter(settings))
    import compiler._

    val scanner = new syntaxAnalyzer.UnitScannerX(
      new CompilationUnit(
        new BatchSourceFile(new VirtualFile("test.scala", ""), s)
      )
    )

    val run = new Run()
    compiler.pushPhase(run.parserPhase)
    scanner.init()

    val iter = scanner.buffer.toIterator
    f(new syntaxAnalyzer.PartialParser(iter))
  }
}


