package scalite

import collection.mutable
import scala.tools.nsc.ast.parser.{Parsers, Scanners}
import scala.tools.nsc.ast.parser.Tokens._
import scala.collection.mutable.ListBuffer

sealed trait Insert
object Insert{
  case class LBraceStack(var baseIndent: Int = 0) extends Insert
  case object RBrace extends Insert
  case object LBrace extends Insert
  case object RParen extends Insert
  case object LParen extends Insert
}

/**
 * Contains the ad-hoc parsing logic required to parse the various forms
 * required by Scalite. This lets us easily identify structures like the
 * headers of classes, functions, etc.. in the stream of tokens.
 */
trait PartialParsers extends Parsers with Scanners { t =>

  val modifierFor: PartialFunction[Stream[Int], PartialParser => Seq[(Int, Insert)]] = {
    case (CLASS | TRAIT) #:: _            => _.classHeader
    case OBJECT #:: _                     => _.objectHeader
    case DEF #:: _                        => _.defHeader
    case (IF | WHILE) #:: LPAREN #:: _    => _.ifWhileHeader
    case (IF | WHILE) #:: _               => _.ifWhileLiteHeader
    case FOR #:: (LPAREN | LBRACE) #:: _  => _.forHeader
    case FOR #:: _                        => _.forLiteHeader
    case (VAL | VAR) #:: _                => _.valVarHeader
    case MATCH #:: _                      => _ => Seq(1 -> Insert.LBraceStack())
  }

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

    import treeBuilder.{global => _, _}

    def valVarHeader = {
      index = 0
      in.nextToken()
      val lhs = commaSeparated(stripParens(noSeq.pattern2()))
      val tp = typedOpt()
      accept(EQUALS)
      Seq(index -> Insert.LBraceStack())
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
      Seq(index -> Insert.LBraceStack())
    }

    def ifWhileHeader = {
      index = 0
      in.nextToken()
      condExpr()
      Seq(index -> Insert.LBraceStack())
    }

    def ifWhileLiteHeader = {
      index = 0

      in.nextToken()
      val startIndex = index
      expr()
      Seq(
        startIndex -> Insert.LParen,
        index -> Insert.RParen,
        index -> Insert.LBraceStack()
      )
    }

    def forHeader = {
      index = 0
      in.nextToken()

      if (in.token == LBRACE) inBracesOrNil(enumerators())
      else inParensOrNil(enumerators())
      newLinesOpt()
      if (in.token == YIELD) {
        in.nextToken()
      }
      Seq(index -> Insert.LBraceStack())
    }
    def forLiteHeader = {
      index = 0

      in.nextToken()
      val startIndex = index

      generator(eqOK = false)
      val endIndex = index
      newLinesOpt()
      if (in.token == YIELD) {
        in.nextToken()
      }
      Seq(
        startIndex -> Insert.LBrace,
        endIndex -> Insert.RBrace,
        index -> Insert.LBraceStack()
      )
    }

    def objectHeader = {
      index = 0
      in.nextToken()
      val name = ident()

      if (in.token == EXTENDS) {
        in.nextToken()
        templateParents()
      }

      Seq(index -> Insert.LBraceStack())
    }

    def classHeader = {
      index = 0
      val isTrait = in.token == TRAIT
      val isCase = in.token == CASECLASS
      in.nextToken()
      val name = ident()

      val contextBoundBuf = new ListBuffer[t.global.Tree]
      typeParamClauseOpt(name, contextBoundBuf)
      if (!isTrait){
        accessModifierOpt()
        paramClauses(name, classContextBounds, ofCaseClass = isCase)
      }

      constructorAnnotations()
      if (in.token == EXTENDS) {
        in.nextToken()
        templateParents()
      }
      Seq(index -> Insert.LBraceStack())
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
