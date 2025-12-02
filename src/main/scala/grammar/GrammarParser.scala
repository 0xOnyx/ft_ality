package grammar

object GrammarParser {
  def readFile(path: String): Either[String, List[String]] = {
    try {
      val source = scala.io.Source.fromFile(path)
      val lines = source.getLines().toList
      source.close()
      Right(lines)
    }
    catch {
      case e: Exception =>
        Left(s"Error reading file: ${e.getMessage}")
    }
  }

  def splitLine(line: String): (String, String) = {
    val parts = line.split(":")
    val name = parts(0).trim
    val sequence = parts(1).trim
    (name, sequence)
  }

  def splitSequence(sequence: String): List[String] = {
    sequence
      .split(",")
      .map(_.trim)
      .toList
  }
  
  def parseRules(path: String): Either[String, List[(String, List[String])]] = {
     readFile(path) match {
       case Left(error) => Left(error)
       case Right(lines) =>
         val rules = lines.map { line =>
           val (name, sequence) = splitLine(line)
           val symbols = splitSequence(sequence)
           (name, symbols)
         }
         Right(rules)
     }
  }
}
