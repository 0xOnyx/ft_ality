package input

import automaton.Automaton

object KeyMapping {

  def displayMapping(mapping: Map[String, String]): Unit = {
    println("Key mappings:")
    println()
    mapping.toList.sortBy(_._1).foreach { case (key, symbol) =>
      println(s"$key -> $symbol")
    }
    println()
  }
}
