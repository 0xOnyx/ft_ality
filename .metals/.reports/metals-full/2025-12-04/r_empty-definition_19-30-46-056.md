error id: file://<WORKSPACE>/src/main/scala/automaton/AutomatonRecognizer.scala:get.
file://<WORKSPACE>/src/main/scala/automaton/AutomatonRecognizer.scala
empty definition using pc, found symbol in pc: get.
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -automation/transitions/get.
	 -automation/transitions/get#
	 -automation/transitions/get().
	 -scala/Predef.automation.transitions.get.
	 -scala/Predef.automation.transitions.get#
	 -scala/Predef.automation.transitions.get().
offset: 611
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
    ): Option[Set[String]] = {
      rest match {
        case Nil =>
          if (automation.finalStates.contains(currentState)) {
            Some(currentState.movements)
          } else {
            None
          }
        case symbol :: remaining =>
          automation.transitions.@@get((currentState, symbol)) match {
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

empty definition using pc, found symbol in pc: get.