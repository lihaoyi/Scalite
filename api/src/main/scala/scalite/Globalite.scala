package scalite

import scala.tools.nsc._
import ast.parser._
import Tokens._
import scala.collection.mutable
import scala.language.postfixOps
import scala.tools.nsc.reporters.Reporter

/**
 * A modified instance of global which lets us intercept the compilation
 * process after the Scanner. This lets us perform a transformation on the
 * stream of tokens before handing it over to the parser to build ASTs
 */
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

  class UnitScannerX(unit: CompilationUnit, patches: List[BracePatch] = Nil)
  extends UnitScanner(unit, patches){
    lazy val buffer = {
      val buffer = mutable.Buffer.empty[ScannerData]
      while (this.token != EOF){
        super.nextToken()
        buffer.append(copyData(this))
      }
      transform(buffer)(unit.source)
    }

    var index = 0
    override def nextToken() = {
      if (index < buffer.length) this.copyFrom(buffer(index))
      index += 1
    }
  }
  class UnitParserX(unit: global.CompilationUnit, patches: List[BracePatch] = Nil)
  extends UnitParser(unit, patches){
    override def newScanner() = new UnitScannerX(unit, patches)
    override def withPatches(patches: List[BracePatch]) = new UnitParserX(unit, patches)
  }
}

