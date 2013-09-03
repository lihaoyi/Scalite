package scalite
import collection.mutable
import scala.tools.nsc.ast.parser.{Parsers, Scanners, Tokens}
import scala.reflect.internal.util.SourceFile

sealed trait Insertion
case object LBrace extends Insertion
case object RBrace extends Insertion
/**
 * Performs transformations on the Scanner's token stream inside the
 * scala compiler
 */
trait Transformer extends Parsers with Scanners with PartialParsers{ t =>
  implicit class pimpedToken(td: ScannerData){
    def pos(implicit source: SourceFile) = source.position(td.offset)
    def col(implicit source: SourceFile) = pos.column
    def line(implicit source: SourceFile) = pos.line
    def prettyPrint = token2string(td.token)
  }

  def transform(input: Seq[ScannerData])(implicit source: SourceFile): Seq[ScannerData] = {

    // Move all newlines to the end of the previous line, rather than the start
    for(i <- input){
       if (i.token == Tokens.NEWLINE || i.token == Tokens.NEWLINES) i.offset -= 1
    }

    val colForLine: Seq[Int] = {
      val arr = new Array[Int](source.content.mkString.lines.length+1)
      for(token <- input){
        if (arr(token.line) == 0){
          arr(token.line) = token.col
        }
      }
      arr
    }

    val insertions = mutable.Seq.fill[List[Insertion]](input.length)(Nil)
    println("tokens")

    input.groupBy(_.line)
         .toList
         .sortBy(_._1)
         .map(_._2.toList)
         .map(x => x.map(x => token2string(x.token)).mkString("\t"))
         .foreach(println)

    println("")
    val stack = mutable.Stack[Int](1)
    def nextLineToken(i: Int) = (input.lift(i+1).map(_.token), input.lift(i+2).map(_.token)) match{
      case (Some(Tokens.NEWLINE | Tokens.NEWLINES), Some(_)) => Some(i+2)
      case (Some(_), _) => Some(i+1)
      case _ => None
    }
    for(i <- 0 until input.length){

      val curr = input(i)
      println(i + "\tloop\t" + stack.top + "\t" + curr.col + "\t" + token2string(curr.token))

      if (input(i).token == Tokens.EOF){
        println("==================EOF======================")
        while(1 < stack.top){
          insertions(i-1) = RBrace :: insertions(i-1)
          println{"POP STACK " + stack.pop()}
        }
      }else {
        for(next <- nextLineToken(i)){
          while(input(next).col < stack.top){
            insertions(i) = RBrace :: insertions(i)
            println{"POP STACK " + stack.pop()}
          }
        }
      }

      for{
        f <- modifierFor.lift(input(i).token)
        offset = f(new PartialParser(input.toIterator.drop(i)))

        last = input(i + offset)
        next <- nextLineToken(i + offset - 1)
        _ = println("offset " + offset)
        _ = println("next " + next)
        if input(next).line > input(i).line
        if input(next).col > colForLine(input(i).line)
      }{
        stack.push(input(next).col)
        println("PUSH STACK " + stack.top)
        insertions(i + offset-1) = LBrace :: insertions(i)
      }
    }

    val merged = mutable.Buffer.empty[ScannerData]
    for(i <- 0 until input.length){
      merged.append(input(i))
      insertions(i).foreach{
        case LBrace =>
          val td = new ScannerData{}
          td.copyFrom(merged.last)
          td.token = Tokens.LBRACE
          merged.append(td)
        case RBrace =>
          val td = new ScannerData{}
          td.copyFrom(merged.last)
          td.token = Tokens.RBRACE
          merged.append(td)
      }
    }

    println("tokens")

    merged.groupBy(_.line)
      .toList
      .sortBy(_._1)
      .map(_._2.toList)
      .map(x => x.map(x => token2string(x.token)).mkString("\t"))
      .foreach(println)

    println("")
    merged
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
  }

}
