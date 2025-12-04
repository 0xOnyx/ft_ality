package automaton

import scala.annotation.tailrec

case class Construction (
                        currentState: Int,
                        states: Set[State],
                        transition: Map[(State, String), State],
                        finalStates: Set[State],
                        )

object AutomatonBuilder {
  def buildAutomaton(rules: List[(String, List[String])]): Automaton = {

    val state0 = State(0, false, Set());
    val initialConstruction = Construction(
      currentState = 0,
      states = Set(state0),
      transition = Map.empty,
      finalStates = Set.empty
    )


    val finalConstruction = rules.foldLeft(initialConstruction){
      (construction, rule) =>
        val (movement, symbols) = rule
        addRule(construction, movement, symbols, state0)
    }

    Automaton(
      state0,
      finalConstruction.finalStates,
      finalConstruction.transition
    )
  }


  def addRule(
               construction: Construction,
               movementName: String,
               symbols: List[String],
               initialState: State
             ): Construction = {

    @tailrec
    def addSequence(
                   const : Construction,
                   currentState: State,
                   rest: List[String]
                   ): (Construction, State) = {
      rest match {
        case Nil =>
          val finalState = currentState.copy(
            isFinal = true,
            movements = currentState.movements + movementName
          )
          val newConstruction = const.copy(
            states = const.states - currentState + finalState,
            finalStates = const.finalStates + finalState
          )
          (newConstruction, finalState)
        case symbol :: remaining =>
          const.transition.get((currentState, symbol)) match {
            case Some(existingState) => 
              addSequence(const, existingState, remaining)
            case None =>
              val newState = State(const.currentState + 1, false, Set())
              val newConstruction = const.copy(
                currentState = const.currentState + 1,
                states = const.states + newState,
                transition = const.transition + ((currentState, symbol) -> newState)
              )
              addSequence(newConstruction, newState, remaining)
          }
      }
    }
    
    val (newConstruction, _) = addSequence(construction, initialState, symbols)
    newConstruction
  }
  
  
}
