package input

import java.io.IOException

// État du terminal (immuable)
case class TerminalState(config: Option[String], isActive: Boolean)

object KeyboardReader {

  // Initialiser le mode raw (retourne l'état du terminal)
  def initialize(): Either[String, TerminalState] = {
    val os = System.getProperty("os.name").toLowerCase

    if (os.contains("win")) {
      // Windows : utiliser jline ou bibliothèque native
      Left("Sur Windows, utilisez jline3 pour le mode raw")
    } else {
      // Linux/Mac : utiliser stty
      try {
        val process = Runtime.getRuntime.exec(Array("sh", "-c", "stty -g"))
        val config = scala.io.Source.fromInputStream(process.getInputStream).mkString.trim
        process.waitFor()

        // Sauvegarder la config puis activer raw
        Runtime.getRuntime.exec(Array("sh", "-c", s"stty -echo raw < /dev/tty")).waitFor()
        Right(TerminalState(Some(config), isActive = true))
      } catch {
        case e: Exception => Left(s"Impossible d'activer le mode raw: ${e.getMessage}")
      }
    }
  }

  // Restaurer le mode normal (retourne un nouvel état)
  def restore(state: TerminalState): TerminalState = {
    if (state.isActive) {
      try {
        state.config match {
          case Some(config) =>
            // Restaurer la configuration sauvegardée
            Runtime.getRuntime.exec(Array("sh", "-c", s"stty $config < /dev/tty")).waitFor()
          case None =>
            // Mode par défaut
            Runtime.getRuntime.exec(Array("sh", "-c", "stty echo cooked < /dev/tty")).waitFor()
        }
        TerminalState(None, isActive = false)
      } catch {
        case _: Exception => TerminalState(None, isActive = false)
      }
    } else {
      state  // Déjà restauré, retourner l'état inchangé
    }
  }

  // Lire une touche (non-bloquant) - FONCTION PURE
  def readKey(): Option[Char] = {
    try {
      if (System.in.available() > 0) {
        Some(System.in.read().toChar)
      } else {
        None
      }
    } catch {
      case _: IOException => None
    }
  }
}