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
    /**
     * Force-offsets `case` tokens one space to the right, so they
     * can be placed vertically aligned with the match clause and
     * still get grouped correctly
     */
    def offsetFor(i: Int) = input(i).token match {
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
        // Close any outstanding things on the stack
        val nextIndex = nextLineToken(i)
        while (!stack.isEmpty && input(nextIndex).col < stack.head.baseIndent + offsetFor(nextIndex)) {
          val head :: tail = stack
          insertions(i) ::= RBrace
          if (head.isInstanceOf[Insert.Stack.LParen]) insertions(i) ::= RParen
          stack = tail
        }

        // Look at whether we need to add new things now or later
        val stream = input.toStream.drop(i)
        for {
          f <- modifierFor.lift(stream.takeWhile(_.line == stream(0).line).map(_.token))
          (checkEolOpt, tokens) = f(new PartialParser(input.toIterator.drop(i), colForLine)(source))

          next = checkEolOpt.fold(input.indexWhere(_.line > stream(0).line))(
            e => nextLineToken(i + e - 1)
          )

          if input(next).line > input(i).line
          if input(next).col > colForLine(input(i).line) + offsetFor(next)
          (offset, token) <- tokens
        } {
          insertions(i + offset - 1) ::= token
          token match {
            case t: Insert.Stack => t.baseIndent = input(next).col - offsetFor(next)
            case _ =>
          }
        }

        // Deal with any new things that need to be added *now*
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

    val merged = for{
      (in, ins) <- input.zip(insertions)
      scannerData <- in :: ins.reverse.flatMap(_.inserts)
                              .map(i => copyData(in, _.token = i))
      if scannerData.token != Tokens.DO
    } yield scannerData

    render(merged)

    merged
  }
}
