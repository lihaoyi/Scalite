package scalite

import scala.tools.nsc._
import ast.parser._
import Tokens._
import scala.collection.mutable
import scala.language.postfixOps
import scala.tools.nsc.reporters.Reporter

class Globalite(settings: Settings, reporter: Reporter) extends Global(settings, reporter){ g =>
  override lazy val syntaxAnalyzer = new {
  val global: g.type = g
  val runsAfter = List[String]()
  val runsRightAfter = None
  } with scalite.SyntaxAnalyzerLite

  override def newUnitScanner(unit: CompilationUnit) = new syntaxAnalyzer.UnitScannerX(unit)
  override def newUnitParser(unit: CompilationUnit) = new syntaxAnalyzer.UnitParserX(unit)
}

abstract class SyntaxAnalyzerLite extends SyntaxAnalyzer with Transformer{
  import global._


  class UnitScannerX(unit: CompilationUnit, patches: List[BracePatch] = Nil) extends UnitScanner(unit, patches){
    lazy val buffer = {
      val buffer = mutable.Buffer.empty[ScannerData]

      while (this.token != EOF){
        super.nextToken()
        val td = new ScannerData{}
        td.copyFrom(this)

        buffer.append(td)

      }

      transform(buffer)(unit.source)
    }

    var index = 0
    override def nextToken() = {
      if (index < buffer.length) this.copyFrom(buffer(index))
      index += 1
    }
  }
  class UnitParserX(unit: global.CompilationUnit, patches: List[BracePatch] = Nil) extends UnitParser(unit, patches){
    override def newScanner() = new UnitScannerX(unit, patches)
    override def withPatches(patches: List[BracePatch]): UnitParser = new UnitParserX(unit, patches)
  }
}

