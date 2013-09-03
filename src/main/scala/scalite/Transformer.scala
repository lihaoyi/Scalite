package scalite
import collection.mutable
import scala.tools.nsc.ast.parser.{Parsers, Scanners, Tokens}
import scala.tools.nsc.ast.parser.Tokens._
import scala.reflect.internal.util.SourceFile
import scala.tools.nsc.Global
import scala.reflect.internal.{ModifierFlags, Trees}
import scala.collection.mutable.ListBuffer



trait Transformer extends Parsers with Scanners { t =>
  implicit class pimpedToken(td: TokenData){
    def pos(implicit source: SourceFile) = source.position(td.offset)
    def col(implicit source: SourceFile) = pos.column
    def line(implicit source: SourceFile) = pos.line
    def prettyPrint = token2string(td.token)
  }
  def transform(input: mutable.Buffer[ScannerData])(implicit source: SourceFile): mutable.Buffer[ScannerData] = {

    val lines = lineify(input)

    insertBraces(lines)

    lines.flatten
  }
  def lineify(input: Seq[ScannerData])(implicit source: SourceFile): mutable.Buffer[mutable.Buffer[ScannerData]] = {

    val lines = mutable.Buffer.empty[mutable.Buffer[ScannerData]]
    var currentLine = 0
    for(token <- input){
      if(token.line > currentLine &&
        token.token != Tokens.NEWLINE &&
        token.token != Tokens.NEWLINES){

        lines.append(mutable.Buffer())
        currentLine = token.line
      }

      lines.last.append(token)
    }
    lines

  }
  def insertBraces(lines: mutable.Buffer[mutable.Buffer[ScannerData]])(implicit source: SourceFile) = {
    val stack = mutable.Stack[Int](1)


    for (i <- 0 until lines.length - 1){
      val line = lines(i)
      val next = lines(i+1)

      // Unwind stack and place closing braces where necessary
      while(next.head.col < stack.top){
        for (token <- Seq(Tokens.RBRACE)){
          val td = new ScannerData{}
          td.copyFrom(line.head)
          td.token = token
          line.last.token match{
            case Tokens.NEWLINE | Tokens.NEWLINES | Tokens.SEMI =>
              line.insert(line.length-1,  td)
            case _ => line.append(td)
          }
        }
        stack.pop()
      }

      for(j <- 0 until line.length) line(j).token match{
        case Tokens.CLASS =>

          val s = tokenStream(i, j)

          s.take(5)
           .map(_.token)
           .map(token2string)
           .foreach(println)
        case _ =>
      }
    }

    def tokenStream(i: Int, j: Int) = {
      val remainingLines = lines.drop(i)
      remainingLines(0) = remainingLines(0).drop(j)
      remainingLines.flatten.toIterator
    }
    def matchHeader(input: Iterator[ScannerData]) = {
      import Tokens._
      val first = input.next()
      first.token match{
        case RETURN | TRY | DO | THROW | FINALLY | MATCH => 1
        case FOR => new PartialParser(Iterator(first) ++ input).forHeader
        case IF | WHILE => new PartialParser(Iterator(first) ++ input).ifWhileHeader
        case CLASS | OBJECT | TRAIT => new PartialParser(Iterator(first) ++ input).declHeader
      }
    }
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
