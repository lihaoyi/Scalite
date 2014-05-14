package scalite
import collection.mutable
import scala.tools.nsc.ast.parser.{Parsers, Scanners, Tokens}
import scala.reflect.internal.util.SourceFile
import scalite.Insert._

/**
 * Performs transformations on the Scanner's token stream inside the
 * scala compiler
 */
trait Transformer extends Parsers with Scanners with PartialParsers{ t =>
  def transform(input: Seq[ScannerData])(implicit source: SourceFile): Seq[ScannerData] = {

    // Move all newlines to the end of the previous line, rather than the start
    for(i <- input){
       if (i.token == Tokens.NEWLINE || i.token == Tokens.NEWLINES) i.offset -= 1
    }

    implicit val colForLine: Seq[Int] = {
      val arr = new Array[Int](source.content.mkString.lines.length+1)
      for(token <- input){
        if (arr(token.line) == 0){
          arr(token.line) = token.col
        }
      }
      arr
    }

    render(input)
    def offsetFor(i: Int) = i match {
      case Tokens.CASE => -1
      case _ => 0
    }

    def nextLineToken(i: Int) = input(i + 1).token match {
      case Tokens.NEWLINE | Tokens.NEWLINES => i + 2
      case _ => i + 1
    }

    val insertions = {
      val insertions = mutable.Seq.fill[List[Insert]](input.length)(Nil)

      var stack = List.empty[Insert.Stack]

      for (i <- 0 until input.length - 1) {
        val next = nextLineToken(i)
        println(Seq(
          token2string(input(next).token),
          !stack.isEmpty,
          input(next).col,
          stack.headOption.map(_.baseIndent),
          offsetFor(input(next).token)
        ).mkString("\t"))

        while (!stack.isEmpty && input(next).col < stack.head.baseIndent + offsetFor(input(next).token)) {
          println("BREAK")
          val head :: tail = stack
          insertions(i) ::= RBrace
          if (head.isInstanceOf[Insert.Stack.LParen]) insertions(i) ::= RParen
          stack = tail
        }

        val stream = input.toStream.drop(i)

        for {
          f <- modifierFor.lift(stream.takeWhile(_.line == stream(0).line).map(_.token))
          tokens = f(new PartialParser(input.toIterator.drop(i), colForLine)(source))
          lastOpt <- tokens.lastOption
          last = input(i + lastOpt._1)
          next = nextLineToken(i + tokens.last._1 - 1)
          if input(next).line > input(i).line
          if input(next).col > colForLine(input(i).line) + offsetFor(input(next).token)
          (offset, token) <- tokens
        } {
          insertions(i + offset - 1) ::= token
          token match {
            case t: Insert.Stack => t.baseIndent = input(next).col - offsetFor(input(next).token)
            case _ =>
          }
        }

        insertions(i).collect {
          case t: Insert.Stack => stack ::= t
        }
      }
      // Close all outstanding braces, making
      // sure to put them before the EOF token
      for(i <- stack) insertions(insertions.length - 2) ::= RBrace

      insertions
    }

    insertions.foreach(println)
    val merged = mutable.Buffer.empty[ScannerData]

    for(i <- 0 until input.length){
      input(i).token match {
        case Tokens.DO =>
        case Tokens.NEWLINE => merged.append(input(i))
        case _ => merged.append(input(i))
      }

      insertions(i).reverse.foreach{
        case Stack.LParen() =>
          for(i <- Seq(Tokens.LPAREN, Tokens.LBRACE)) {
            merged.append(copyData(merged.last, _.token = i))
          }
        case Stack.LBraceCase() | Stack.LBrace() | LBrace =>
          merged.append(copyData(merged.last, _.token = Tokens.LBRACE))

        case RBrace => merged.append(copyData(merged.last, _.token = Tokens.RBRACE))
        case RParen => merged.append(copyData(merged.last, _.token = Tokens.RPAREN))
        case LParen => merged.append(copyData(merged.last, _.token = Tokens.LPAREN))
      }
    }

    render(merged)

    merged
  }
}
