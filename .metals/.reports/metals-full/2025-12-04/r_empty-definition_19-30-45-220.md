error id: file://<WORKSPACE>/src/main/scala/automaton/AutomatonRecognizer.scala:scala/Predef.String#
file://<WORKSPACE>/src/main/scala/automaton/AutomatonRecognizer.scala
empty definition using pc, found symbol in pc: scala/Predef.String#
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -String#
	 -scala/Predef.String#
offset: 338
uri: file://<WORKSPACE>/src/main/scala/automaton/AutomatonRecognizer.scala
text:
```scala
package automaton

import scala.annotation.tailrec

object AutomatonRecognizer {

  def reconizer(
                 automation: Automaton,
                 sequence: List[String]
               ): Option[Set[String]] = {
    
    @tailrec
    def recognizeLoop(
         currentState: State,
         rest: List[String]
    ): Option[Set[@@String]] = {
      rest match {
        case Nil =>
          if (automation.finalStates.contains(currentState)) {
            Some(currentState.movements)
          } else {
            None
          }
        case symbol :: remaining =>
          automation.transitions.get((currentState, symbol)) match {
            case Some(newState) =>
              recognizeLoop(newState, remaining)
            case None =>
              None
          }
      }
    }
    
    recognizeLoop(automation.initialState, sequence)
    
  }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: scala/Predef.String#