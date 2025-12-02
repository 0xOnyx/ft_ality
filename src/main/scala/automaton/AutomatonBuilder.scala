package automaton

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
        val (movementname, symbols) = rule
        addRule(construction, movementname, symbols, state0)
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
  
  
  }
}
