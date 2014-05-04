package scalite

import utest._
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.Settings
import scala.reflect.internal.util.{BatchSourceFile, SourceFile}
import scala.reflect.io.VirtualFile
import scala.tools.nsc.ast.parser.Tokens


class PartialParsing extends TestSuite{
  import TestUtils._

  val tests = TestSuite{
    'objects{
      def check(s: String, n: Int) = assert(parsePartial(_.objectHeader, s) == n)
      check("object X", 2)
      check("object X extends Cow", 4)
      check("object X extends Y(1) with Z{ val x = 10 }", 9)
    }

    'classes{
      def check(s: String, n: Int) = assert(parsePartial(_.classHeader, s) == n)
      check("class X", 2)
      check("class X{val x = 1}", 2)
      check("class X[T, U: V](a: A, b: V) extends C(a, b) with D{val x = 1}", 27)
      check("class \n\nX[T\n,\n\n U:\n\n V](\na\n:\n A,\n\n b:\n\n V) extends \n\nC(a, b) with\n D{val x = 1}", 27)
      check("trait \n\nX[T\n,\n\n U:\n\n V] extends \n\nC with\n D{val x = 1}", 13)
      check("class X\n", 2)
    }

    'defs{
      def check(s: String, n: Int) = assert(parsePartial(_.defHeader, s) == n)
      check("def x = 10", 3)
      check("def x[T](a: Int, b: String): T = 10", 17)
      check("def\n x[\n\nT\n](a\n\n: Int\n, \nb: String)\n\n: T \n= {x; y; 10}", 17)
    }

    'valvar{
      def check(s: String, n: Int) = assert(parsePartial(_.valVarHeader, s) == n)
      check("val x = 10", 3)
      check("val (x, y) = (1, 2)", 7)
      check("val x, y = 3", 5)
      check("val x, (y, z)\n = \n{3}", 9)
    }

    'ifelse{
      def check(s: String, n: Int) = assert(parsePartial(_.ifWhileHeader, s) == n)
      check("if (true) 1 else 2", 4)
      check("if \n(true           )\n\n\n 1 else 2", 4)
      check("while (true == false) 1", 6)
      check("while \n\n\n(\n\ntrue\n          == false) 1", 6)
      check("if(if(true) 1 else 2) 1 else 2", 10)
    }

    'forloops{
      def check(s: String, n: Int) = assert(parsePartial(_.forHeader, s) == n)
      check("for (x <- xs) 1 ", 6)
      check("for {x <- xs} 1 ", 6)
      check("for (x <- xs) yield 1 ", 7)
      check("for (x <- for{y <- z} 1) yield 1 ", 13)
      check("for \n\n{x <- xs\ny <- ys\n\n} yield 1 ", 11)
    }
  }


  def parsePartial[T](f: Transformer#PartialParser => T, s: String):T  = {
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


