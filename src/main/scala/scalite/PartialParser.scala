package scalite

import collection.mutable
import scala.tools.nsc.ast.parser.{Parsers, Scanners, Tokens}
import scala.tools.nsc.ast.parser.Tokens._
import scala.reflect.internal.util.SourceFile
import scala.collection.mutable.ListBuffer

/**
 * Contains the ad-hoc parsing logic required to parse the various forms
 * required by Scalite. This lets us easily identify structures like the
 * headers of classes, functions, etc.. in the stream of tokens.
 */
trait PartialParsers extends Parsers with Scanners { t =>

  class PartialParser(inputDontUseMe: Iterator[ScannerData]) extends SourceFileParser(null) {
    val global = t.global
    var index = 0

    override def newScanner(): Scanner = new Scanner{
      def error(off: Int, msg: String) {}
      def incompleteInputError(off: Int, msg: String) {}
      def deprecationWarning(off: Int, msg: String) {}
      val buf: Array[Char] = Array()
      override def nextToken() = {
        index += 1
        this.copyFrom(inputDontUseMe.next())
      }
    }

    def declHeader = {
      index = 0
      val d = tmplDef(0, modifiers())
      index
    }
    import treeBuilder.{global => _, _}

    def valVarHeader = {
      index = 0
      in.nextToken()
      val lhs = commaSeparated(stripParens(noSeq.pattern2()))
      val tp = typedOpt()
      accept(EQUALS)
      index
    }

    private var classContextBounds: List[t.global.Tree] = Nil
    @inline private def savingClassContextBounds[T](op: => T): T = {
      val saved = classContextBounds
      try op
      finally classContextBounds = saved
    }

    def defHeader = {
      index = 0
      in.nextToken()
      if (in.token == THIS) {
        val vparamss = paramClauses(t.global.nme.CONSTRUCTOR, classContextBounds map (_.duplicate), ofCaseClass = false)
        typedOpt()
        accept(EQUALS)
      } else {
        val nameOffset = in.offset
        val name = ident()
        val contextBoundBuf = new ListBuffer[t.global.Tree]
        val tparams = typeParamClauseOpt(name, contextBoundBuf)
        val vparamss = paramClauses(name, contextBoundBuf.toList, ofCaseClass = false)
        typedOpt()
        accept(EQUALS)
      }
      index
    }

    def ifWhileHeader = {
      index = 0
      in.nextToken()
      condExpr()
      index
    }

    def forHeader = {
      index = 0
      in.nextToken()
      val enums =
        if (in.token == LBRACE) inBracesOrNil(enumerators())
        else inParensOrNil(enumerators())
      newLinesOpt()
      if (in.token == YIELD) {
        in.nextToken()
      }
      index
    }


    def objectHeader = {
      index = 0
      in.nextToken()
      val name = ident()

      if (in.token == EXTENDS) {
        in.nextToken()
        templateParents()
      }

      index
    }

    def classHeader = {
      index = 0
      val isTrait = in.token == TRAIT
      val isCase = in.token == CASECLASS
      in.nextToken()
      val name = ident()

      val contextBoundBuf = new ListBuffer[t.global.Tree]
      val tparams = typeParamClauseOpt(name, contextBoundBuf)
      if (!isTrait){
        accessModifierOpt()
        paramClauses(name, classContextBounds, ofCaseClass = isCase)
      }

      constructorAnnotations()
      if (in.token == EXTENDS) {
        in.nextToken()
        templateParents()
      }
      index
    }
    def parseAll = {
      val buff = mutable.Buffer.empty[TokenData]
      do{
        val td = new ScannerData{}
        td.copyFrom(in)
        in.nextToken()
        buff.append(td)
      }while(in.token != EOF)
      buff
    }
  }
}
