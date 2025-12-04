error id: file://<WORKSPACE>/src/main/scala/input/MainLoop.scala:`<none>`.
file://<WORKSPACE>/src/main/scala/input/MainLoop.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -automaton/State#
	 -State#
	 -scala/Predef.State#
offset: 263
uri: file://<WORKSPACE>/src/main/scala/input/MainLoop.scala
text:
```scala
package input

import automaton.{Automaton, State}
import scala.annotation.tailrec

object MainLoop {

  val COMBO_DELAY_MS = 300

  def mainloop(automaton: Automaton, mapping: Map[String, String]): Unit = {

    @tailrec
    def loop(
            currentState: S@@tate,
            buffer: List[String],
            lastTime: Long = System.currentTimeMillis()
            ): Unit = {
      val now = System.currentTimeMillis()
      val timeSinceLastKey = now - lastTime

      if (automaton.finalStates.contains(currentState) && timeSinceLastKey > COMBO_DELAY_MS ){
        println(buffer.mkString(", "))
        println()

        currentState.movements.foreach { movement =>
          println(s"Executing movement: $movement")
        }

        println()
        loop(automaton.initialState, List.empty, now)
      }
      else {
        val key = KeyboardReader.readKey()
        
        key match {
          case Some(keychar) =>
            val keyStr = keychar.toString.toLowerCase()

            mapping.get(keyStr) match {
              case Some(symbol) =>
                val newBuffer = buffer :+ symbol

                automaton.transitions.get((currentState, symbol)) match {
                  case Some(newState) =>
                    loop(newState, newBuffer, now)
                  case None =>
                    loop(automaton.initialState, List.empty, now)
                }
              case None =>
                loop(currentState, buffer, lastTime)
            }

          case None =>
            Thread.sleep(10)
            loop(currentState, buffer, lastTime)
        }
      }
    }

    loop(automaton.initialState, List.empty)
  }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.