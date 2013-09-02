package scalite
import collection.mutable
import scala.tools.nsc.ast.parser.{Parsers, Scanners, Tokens}
import scala.tools.nsc.ast.parser.Tokens._
import scala.reflect.internal.util.SourceFile
import scala.tools.nsc.Global
import scala.reflect.internal.Trees


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

          println("woo")
          println(new PartialParser(tokenStream(i, j)).declHeader)
          println("omg")
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
  }

  class FakeScanner(input: Iterator[ScannerData]) extends Scanner{
    def error(off: Int, msg: String) {}
    def incompleteInputError(off: Int, msg: String) {}
    def deprecationWarning(off: Int, msg: String) {}
    val buf: Array[Char] = Array()

    override def nextToken() = this.copyFrom(input.next())
  }

  class PartialParser(inputDontUseMe: Iterator[ScannerData]) extends SourceFileParser(null) {
    val global = t.global
    var index = 0

    override def newScanner(): Scanner = new FakeScanner(inputDontUseMe.map{x => index += 1; x})

    def declHeader = {
      index = 0
      tmplDef(0, modifiers())
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
    override def templateBody(isPre: Boolean) = (t.global.emptyValDef, Nil)


  }
}
