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

  def parseMapping(line: String): Option[(String, String)] = {
    if (line.contains("->")) {
      val parts = line.split("->")
      if (parts.length == 2) {
        Some((parts(0).trim, parts(1).trim))
      } else {
        None
      }
    } else {
      None
    }
  }

  def separateMappingsAndRules(lines: List[String]): (List[(String, String)], List[String]) = {
    // Fonction récursive pour séparer (FONCTIONNEL)
    @scala.annotation.tailrec
    def separateLoop(
                      remaining: List[String],
                      mappings: List[(String, String)],
                      rules: List[String],
                      inMappingSection: Boolean
                    ): (List[(String, String)], List[String]) = {
      remaining match {
        case Nil =>
          (mappings, rules)
        case line :: rest =>
          val trimmed = line.trim
          if (trimmed.isEmpty) {
            // Ligne vide : passer à la section règles
            separateLoop(rest, mappings, rules, inMappingSection = false)
          } else if (trimmed.contains("->")) {
            // Ligne de mapping
            parseMapping(line) match {
              case Some(mapping) =>
                separateLoop(rest, mappings :+ mapping, rules, inMappingSection = true)
              case None =>
                separateLoop(rest, mappings, rules, inMappingSection)
            }
          } else if (inMappingSection && !trimmed.contains(":")) {
            // Encore dans la section mapping mais pas de "->" : ignorer
            separateLoop(rest, mappings, rules, inMappingSection)
          } else {
            // Ligne de règle (contient ":")
            separateLoop(rest, mappings, rules :+ line, inMappingSection = false)
          }
      }
    }

    separateLoop(lines, List.empty, List.empty, inMappingSection = true)
  }


  def parseRules(path: String): Either[String, (Map[String, String], List[(String, List[String])])] = {

    readFile(path).map { lines =>
      val (mappings, ruleLines) = separateMappingsAndRules(lines)
      val rules = ruleLines.map { line =>
        val (name, sequence) = splitLine(line)
        val symbols = splitSequence(sequence)
        (name, symbols)
      }
      (mappings.toMap, rules)
    }
  }

}
