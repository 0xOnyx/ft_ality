package automaton


case class State(
               id: Int,
               isFinal: Boolean,
               movements: Set[String]
               )


case class Automaton(
                initialState: State,
                finalStates: Set[State],
                transitions: Map[(State, String), State]
                )
