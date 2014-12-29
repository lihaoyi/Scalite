package scalite

import collection.mutable
import scala.tools.nsc.ast.parser.{Parsers, Scanners}
import scala.tools.nsc.ast.parser.Tokens._
import scala.collection.mutable.ListBuffer
import scala.reflect.internal.util.SourceFile

sealed class Insert(val inserts: Int*)
object Insert{
  sealed class Stack(inserts: Int*) extends Insert(inserts:_*){
    var baseIndent: Int = 0
  }

  object Stack{
    case class LBrace() extends Stack(LBRACE)
    case class LParen() extends Stack(LPAREN, LBRACE)
  }

  case object RBrace extends Insert(RBRACE)
  case object LBrace extends Insert(LBRACE)
  case object RParen extends Insert(RPAREN)
  case object LParen extends Insert(LPAREN)
}

/**
 * Contains the ad-hoc parsing logic required to parse the various forms
 * required by Scalite. This lets us easily identify structures like the
 * headers of classes, functions, etc.. in the stream of tokens.
 */
trait PartialParsers extends Parsers with Scanners { t =>

  def copyData(sd: ScannerData, ops: (ScannerData => Unit)*) = {
    val td = new ScannerData{}
    td.copyFrom(sd)
    ops.map(_(td))
    td
  }
  def render(input: Seq[ScannerData])
            (implicit source: SourceFile, colForLine: Seq[Int]) = {
//    println("tokens")
//    input.groupBy(_.line)
//         .toList
//         .sortBy(_._1)
//         .map{ case (line, x) =>
//            " " * colForLine(line) + x.map(x => token2string(x.token)).mkString("\t")
//          }
//         .foreach(println)
//    println("")
  }
  implicit class pimpedToken(td: TokenData){
    def pos(implicit source: SourceFile) = source.position(td.offset)
    def col(implicit source: SourceFile) = pos.column
    def line(implicit source: SourceFile) = pos.line
    def prettyPrint = token2string(td.token)
  }

  import Stream.Empty
  val modifierFor: PartialFunction[Stream[Int], PartialParser => (Option[Int], Seq[(Int, Insert)])] = {
    case DO #:: _                                => _ => None -> Seq(1 -> Insert.Stack.LBrace())
    case (CLASS | TRAIT) #:: _                   => _.classHeader
    case (OBJECT | CASEOBJECT) #:: _             => _.objectHeader
    case DEF #:: _                               => _.defHeader
    case FOR #:: Empty                           => _ => Some(1) -> Seq(1 -> Insert.Stack.LBrace())
    case (IF | WHILE) #:: Empty                  => _ => Some(1) -> Seq(1 -> Insert.Stack.LParen())
    case (TRY | ELSE | YIELD) #:: Empty          => _ => Some(1) -> Seq(1 -> Insert.Stack.LBrace())
    case (IF | WHILE) #:: LPAREN #:: _           => _.ifWhileHeader
    case (IF | WHILE) #:: _                      => _.ifWhileLiteHeader
    case (MATCH | CATCH) #:: Empty               => _ => Some(1) -> Seq(1 -> Insert.Stack.LBrace())
    case FOR #:: (LPAREN | LBRACE) #:: _         => _.forHeader
    case FOR #:: _                               => _.forLiteHeader
    case (VAL | VAR) #:: _                       => _.valVarHeader
  }

  class PartialParser(inputDontUseMe: Iterator[ScannerData], colForLine: Seq[Int])(implicit source: SourceFile) extends SourceFileParser(null) {
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

    def identBlock = {
      println("IDENT BLOCK")

      index = 0
      in.nextToken()
      val (cLine, cToken) = (in.line, in.token)
      in.nextToken()
      val (nLine, nToken) = (in.line, in.token)
      if (nLine > cLine && colForLine(nLine) > colForLine(cLine)) {
        Seq(2 -> Insert.Stack.LBrace())
      }
      else Nil
    }
    def valVarHeader = {
      index = 0
      in.nextToken()
      val lhs = commaSeparated(stripParens(noSeq.pattern2()))
      val tp = typedOpt()

      in.token match{
        case EQUALS =>
          in.nextToken()
          Some(index) -> Seq(index -> Insert.Stack.LBrace())
        case _ => None -> Nil
      }
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
      } else {
        val nameOffset = in.offset
        val name = ident()
        val contextBoundBuf = new ListBuffer[t.global.Tree]
        val tparams = typeParamClauseOpt(name, contextBoundBuf)
        val vparamss = paramClauses(name, contextBoundBuf.toList, ofCaseClass = false)
        typedOpt()
      }

      in.token match{
        case EQUALS =>
          in.nextToken()
          Some(index) -> Seq(index -> Insert.Stack.LBrace())
        case _ => None -> Nil
      }
    }

    def ifWhileHeader = {
      index = 0
      in.nextToken()
      condExpr()
      Some(index) -> Seq(index -> Insert.Stack.LBrace())
    }

    def ifWhileLiteHeader = {
      index = 0
      in.nextToken()
      val startIndex = index
      stripParens(postfixExpr())
      in.token match{
        case ARROW => None -> Nil
        case _ =>
          Some(index) -> Seq(
            startIndex -> Insert.LParen,
            index -> Insert.RParen,
            index -> Insert.Stack.LBrace()
          )
      }
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
      Some(index) -> Seq(index -> Insert.Stack.LBrace())
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
      Some(index) -> Seq(
        startIndex -> Insert.LBrace,
        endIndex -> Insert.RBrace,
        index -> Insert.Stack.LBrace()
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

      Some(index) -> Seq(index -> Insert.Stack.LBrace())
    }

    def classHeader = {
      index = 0
      val isTrait = in.token == TRAIT
      val isCase = in.token == CASECLASS

      in.nextToken()
      val name = ident().toTypeName

      val contextBoundBuf = new ListBuffer[t.global.Tree]
      val c = typeParamClauseOpt(name, contextBoundBuf)

      if (!isTrait){
        accessModifierOpt()
        paramClauses(name, classContextBounds, ofCaseClass = isCase)
      }

      constructorAnnotations()
      if (in.token == EXTENDS) {
        in.nextToken()
        templateParents()
      }
      Some(index) -> Seq(index -> Insert.Stack.LBrace())
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
