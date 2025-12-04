import automaton.{Automaton, AutomatonBuilder}
import grammar.GrammarParser
import input.{KeyMapping, KeyboardReader, MainLoop}

@main
def main(filePath: String): Unit = {
  GrammarParser.parseRules(filePath) match {
    case Right((mappings, rules)) =>
      val automaton = AutomatonBuilder.buildAutomaton(rules)

      KeyMapping.displayMapping(mappings)
      println("----------------------")
      println()

      KeyboardReader.initialize() match {
        case Right(terminalState) =>
          MainLoop.mainloop(automaton, mappings)
        case Left(error) =>
          println(s"Error initializing keyboard reader: $error")
      }

      sys.exit(1)

    case Left(error) =>
      println(error)
      sys.exit(1)
  }
}

