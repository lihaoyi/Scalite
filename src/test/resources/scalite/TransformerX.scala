package scalite
import collection.mutable
import scala.tools.nsc.ast.parser.{Scanners, Tokens}
import scala.tools.nsc.ast.parser.Tokens._
import scala.reflect.internal.util.SourceFile

class TransformerX
  def apply() = "10"

trait TransformerY extends Scanners
  implicit class pimpedToken(td: TokenData)
    def pos(implicit source: SourceFile) = source.position(td.offset)
    def col(implicit source: SourceFile) = pos.column
    def line(implicit source: SourceFile) = pos.line
    def prettyPrint = token2string(td.token)

  def transform(input: mutable.Buffer[TokenData])(implicit source: SourceFile): mutable.Buffer[TokenData] =

    val lines = lineify(input)
    for((tokens, row) <- lines.zipWithIndex)
      println(row + ":" + tokens(0).col + ";\t" + tokens.map(_.prettyPrint).mkString("    "))

    println("insertBraces")
    insertBraces(lines)
    for((tokens, row) <- lines.zipWithIndex)
      println(row + ":" + tokens(0).col + ";\t" + tokens.map(_.prettyPrint).mkString("    "))


    lines.flatten

  def lineify(input: Seq[TokenData])(implicit source: SourceFile): mutable.Buffer[mutable.Buffer[TokenData]] =

    val lines = mutable.Buffer.empty[mutable.Buffer[TokenData]]
    var currentLine = 0
    for(token <- input)
      if(token.line > currentLine && token.token != Tokens.NEWLINE && token.token != Tokens.NEWLINES)

        lines.append(mutable.Buffer())
        currentLine = token.line


      lines.last.append(token)

    lines


  def insertBraces(lines: mutable.Buffer[mutable.Buffer[TokenData]])(implicit source: SourceFile) =
    val stack = mutable.Stack[Int](1)

    for (i <- 0 until lines.length - 1)
      val line = lines(i)
      val next = lines(i+1)


      while(next.head.col < stack.top)
        for (token <- Seq(Tokens.RBRACE))
          val td = new TokenData{}
          td.copyFrom(line.head)
          td.token = token
          line.last.token match
            case Tokens.NEWLINE | Tokens.NEWLINES | Tokens.SEMI =>
              line.insert(line.length-1,  td)
            case _ => line.append(td)

        stack.pop()

      if (line.head.col < next.head.col)
        line.last.token match
          case Tokens.NEWLINE | Tokens.NEWLINES =>
            stack.push(next.head.col)
            val td = new TokenData{}
            td.copyFrom(line.last)
            td.token = Tokens.LBRACE
            line.insert(line.length-1, td)
          case Tokens.DO =>
            stack.push(next.head.col)
            line.last.token = Tokens.LBRACE

          case Tokens.EQUALS | Tokens.MATCH =>
            stack.push(next.head.col)
            val td = new TokenData{}
            td.copyFrom(line.last)
            td.token = Tokens.LBRACE
            line.append(td)
          case Tokens.ARROW | Tokens.LBRACE | Tokens.ELSE =>

