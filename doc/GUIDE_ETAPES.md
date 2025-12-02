# Guide étape par étape : Implémenter ft_ality

Ce guide te montre **exactement** ce qu'il faut faire, dans quel ordre, pour créer le projet ft_ality.

## ⚠️ IMPORTANT : Programmation fonctionnelle pure

**RÈGLES ABSOLUES** :
- ❌ **JAMAIS de `var`** : Utiliser uniquement `val`
- ❌ **JAMAIS de `while`** : Utiliser la récursion terminale (`@tailrec`)
- ❌ **JAMAIS de mutation** : Créer de nouvelles structures au lieu de modifier
- ✅ **Fonctions pures** : Pas d'effets de bord (sauf I/O nécessaire)
- ✅ **Récursion terminale** : Toutes les boucles sont en récursion optimisée
- ✅ **Structures immutables** : `List`, `Set`, `Map`, case classes uniquement

---

## 📋 Table des matières

1. [Préparation](#préparation)
2. [Étape 1 : Structure du projet](#étape-1--structure-du-projet)
3. [Étape 2 : Définir les types de base](#étape-2--définir-les-types-de-base)
4. [Étape 3 : Lire et parser le fichier](#étape-3--lire-et-parser-le-fichier)
5. [Étape 4 : Construire l'automate](#étape-4--construire-lautomate)
6. [Étape 5 : Reconnaître des séquences](#étape-5--reconnaître-des-séquences)
7. [Étape 6 : Lire les touches en temps réel](#étape-6--lire-les-touches-en-temps-réel)
8. [Étape 7 : Key mapping automatique](#étape-7--key-mapping-automatique)
9. [Étape 8 : Boucle principale](#étape-8--boucle-principale)
10. [Étape 9 : Tout assembler](#étape-9--tout-assembler)
11. [Étape 10 : Tests](#étape-10--tests)

---

## 🚀 Préparation

### ⚠️ Rappel : Programmation fonctionnelle pure

**Tout le code doit respecter ces règles** :
- ❌ **JAMAIS de `var`** : Utiliser uniquement `val`
- ❌ **JAMAIS de `while`** : Utiliser la récursion terminale (`@tailrec`)
- ❌ **JAMAIS de mutation** : Créer de nouvelles structures au lieu de modifier
- ✅ **Fonctions pures** : Pas d'effets de bord (sauf I/O nécessaire)
- ✅ **Récursion terminale** : Toutes les boucles sont en récursion optimisée
- ✅ **Structures immutables** : `List`, `Set`, `Map`, case classes uniquement
- ✅ **Gestion d'erreurs** : Utiliser `Either` ou `Option`, pas d'exceptions

### 1. Créer la structure de base

```bash
# Créer les dossiers
mkdir -p src/main/scala/{automaton,grammar,input,output}
mkdir -p grammars
mkdir -p doc
```

### 2. Créer `build.sbt`

Créer le fichier `build.sbt` à la racine du projet :

```scala
ThisBuild / scalaVersion := "3.3.0"

lazy val root = (project in file("."))
  .settings(
    name := "ft_ality",
    version := "1.0.0",
    libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1"
  )
```

### 3. Créer un fichier de grammaire de test

Créer `grammars/test.gmr` :

```
Punch: [BP]
Combo: [BP], [FP]
Kick: [FP]
```

---

## 📝 Étape 1 : Structure du projet

### Fichiers à créer dans cet ordre :

```
src/main/scala/
├── main.scala                    # Point d'entrée (CRÉER EN DERNIER)
├── automaton/
│   └── State.scala              # Commencer par ça
├── grammar/
│   └── GrammarParser.scala      # Puis ça
├── input/
│   └── KeyboardReader.scala     # Ensuite ça
└── output/
    └── (rien pour l'instant)
```

**Action** : Créer ces dossiers maintenant.

---

## 📝 Étape 2 : Définir les types de base

### Créer `automaton/State.scala`

```scala
package automaton

// Un état dans l'automate
case class State(
  id: Int,                    // Numéro de l'état (0, 1, 2, ...)
  isFinal: Boolean,          // true si c'est un état final
  movements: Set[String]    // Noms des mouvements reconnus
)

// L'automate complet
case class Automaton(
  initialState: State,                              // État de départ
  finalStates: Set[State],                         // États où on reconnaît
  transitions: Map[(State, String), State]          // Les flèches : (état_départ, symbole) -> état_arrivée
)
```

**Action** :
1. ✅ Créer le fichier `src/main/scala/automaton/State.scala`
2. ✅ Copier le code ci-dessus
3. ✅ Vérifier que ça compile : `sbt compile`

---

## 📝 Étape 3 : Lire et parser le fichier

### Créer `grammar/GrammarParser.scala`

```scala
package grammar

object GrammarParser {
  
  // Lire un fichier ligne par ligne
  def readFile(path: String): Either[String, List[String]] = {
    try {
      val source = scala.io.Source.fromFile(path)
      val lines = source.getLines().toList
      source.close()
      Right(lines)
    } catch {
      case e: Exception =>
        Left(s"Erreur : ${e.getMessage}")
    }
  }
  
  // Découper une ligne en nom et séquence
  def splitLine(line: String): (String, String) = {
    val parts = line.split(":")
    val name = parts(0).trim
    val sequence = parts(1).trim
    (name, sequence)
  }
  
  // Découper une séquence en symboles
  def splitSequence(sequence: String): List[String] = {
    sequence
      .split(",")
      .map(_.trim)
      .toList
  }
  
  // Parser toutes les règles du fichier
  def parseRules(path: String): Either[String, List[(String, List[String])]] = {
    readFile(path).map { lines =>
      lines.map { line =>
        val (name, sequence) = splitLine(line)
        val symbols = splitSequence(sequence)
        (name, symbols)
      }
    }
  }
}
```

**Action** :
1. ✅ Créer le fichier `src/main/scala/grammar/GrammarParser.scala`
2. ✅ Copier le code ci-dessus
3. ✅ Tester : créer un petit test dans `main.scala` temporairement
4. ✅ Vérifier que ça compile

---

## 📝 Étape 4 : Construire l'automate

### Créer `automaton/AutomatonBuilder.scala`

```scala
package automaton

import scala.annotation.tailrec

// Structure pour garder l'état de construction
case class Construction(
  currentState: Int,                                    // Prochain numéro d'état
  states: Set[State],                                    // Tous les états créés
  transitions: Map[(State, String), State],             // Toutes les transitions
  finalStates: Set[State]                              // Tous les états finaux
)

object AutomatonBuilder {
  
  // Construire l'automate à partir d'une liste de règles
  def buildAutomaton(rules: List[(String, List[String])]): Automaton = {
    // 1. Créer l'état initial
    val state0 = State(0, false, Set())
    val initialConstruction = Construction(
      currentState = 0,
      states = Set(state0),
      transitions = Map.empty,
      finalStates = Set.empty
    )
    
    // 2. Ajouter chaque règle une par une (avec foldLeft)
    val finalConstruction = rules.foldLeft(initialConstruction) { (construction, rule) =>
      val (movementName, symbols) = rule
      addRule(construction, movementName, symbols, state0)
    }
    
    // 3. Retourner l'automate
    Automaton(state0, finalConstruction.finalStates, finalConstruction.transitions)
  }
  
  // Ajouter une règle à la construction
  def addRule(
    construction: Construction,
    movementName: String,
    symbols: List[String],
    initialState: State
  ): Construction = {
    // Fonction récursive pour parcourir les symboles
    @tailrec
    def addSequence(
      const: Construction,
      currentState: State,
      rest: List[String]
    ): (Construction, State) = {
      rest match {
        case Nil =>
          // Fin de la séquence : marquer l'état comme final
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
          // Vérifier si une transition existe déjà
          const.transitions.get((currentState, symbol)) match {
            case Some(existingState) =>
              // Transition existe : réutiliser l'état
              addSequence(const, existingState, remaining)
            case None =>
              // Pas de transition : créer un nouvel état
              val newState = State(const.currentState + 1, false, Set())
              val newConstruction = const.copy(
                currentState = const.currentState + 1,
                states = const.states + newState,
                transitions = const.transitions + ((currentState, symbol) -> newState)
              )
              addSequence(newConstruction, newState, remaining)
          }
      }
    }
    
    val (newConstruction, _) = addSequence(construction, initialState, symbols)
    newConstruction
  }
}
```

**Action** :
1. ✅ Créer le fichier `src/main/scala/automaton/AutomatonBuilder.scala`
2. ✅ Copier le code ci-dessus
3. ✅ Vérifier que ça compile
4. ✅ Tester avec `grammars/test.gmr` pour voir si l'automate se construit

---

## 📝 Étape 5 : Reconnaître des séquences

### Créer `automaton/AutomatonRecognizer.scala`

```scala
package automaton

import scala.annotation.tailrec

object AutomatonRecognizer {
  
  // Reconnaître une séquence (FONCTIONNEL)
  def recognize(automaton: Automaton, sequence: List[String]): Option[Set[String]] = {
    // Fonction récursive interne
    @tailrec
    def recognizeLoop(
      currentState: State,
      rest: List[String]
    ): Option[Set[String]] = {
      rest match {
        case Nil =>
          // Fin de la séquence : vérifier si on est dans un état final
          if (automaton.finalStates.contains(currentState)) {
            Some(currentState.movements)  // ✅ Reconnu !
          } else {
            None  // ❌ Non reconnu
          }
          
        case symbol :: remaining =>
          // Chercher la transition
          automaton.transitions.get((currentState, symbol)) match {
            case Some(newState) =>
              // Transition trouvée : continuer avec le nouvel état
              recognizeLoop(newState, remaining)
            case None =>
              // Pas de transition : la séquence n'est pas reconnue
              None
          }
      }
    }
    
    // Commencer la récursion depuis l'état initial
    recognizeLoop(automaton.initialState, sequence)
  }
}
```

**Action** :
1. ✅ Créer le fichier `src/main/scala/automaton/AutomatonRecognizer.scala`
2. ✅ Copier le code ci-dessus
3. ✅ Tester avec une séquence simple comme `List("[BP]")`

---

## 📝 Étape 6 : Lire les touches en temps réel

### Créer `input/KeyboardReader.scala`

```scala
package input

import java.io.IOException

// État du terminal (immuable)
case class TerminalState(config: Option[String], isActive: Boolean)

// Lire les touches en temps réel (sans Entrée) - VERSION FONCTIONNELLE PURE
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
```

**Action** :
1. ✅ Créer le fichier `src/main/scala/input/KeyboardReader.scala`
2. ✅ Copier le code ci-dessus
3. ⚠️ **Note** : Sur Windows, il faudra utiliser une bibliothèque comme `jline3`
4. ✅ **Important** : Cette version est 100% fonctionnelle (pas de `var`, état passé en paramètre)

---

## 📝 Étape 7 : Key mapping automatique

### Créer `input/KeyMapping.scala`

```scala
package input

import automaton.Automaton

object KeyMapping {
  
  // Créer le key mapping automatiquement
  def createKeyMapping(automaton: Automaton): Map[String, String] = {
    // 1. Extraire tous les symboles de l'automate
    val symbols = automaton.transitions.keys.map(_._2).toSet.toList
    
    // 2. Liste des touches disponibles
    val keys = List("q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
                       "a", "s", "d", "f", "g", "h", "j", "k", "l",
                       "z", "x", "c", "v", "b", "n", "m")
    
    // 3. Associer chaque symbole à une touche
    symbols.zip(keys).map(_.swap).toMap
  }
  
  // Afficher les mappings
  def displayMappings(mapping: Map[String, String]): Unit = {
    println("Key mappings:")
    println()
    mapping.toList.sortBy(_._1).foreach { case (key, symbol) =>
      println(s"$key -> $symbol")
    }
    println()
  }
}
```

**Action** :
1. ✅ Créer le fichier `src/main/scala/input/KeyMapping.scala`
2. ✅ Copier le code ci-dessus
3. ✅ Vérifier que ça compile

---

## 📝 Étape 8 : Boucle principale

### Créer `input/MainLoop.scala`

```scala
package input

import automaton.{Automaton, State}
import scala.annotation.tailrec

object MainLoop {
  
  // Constante : délai en millisecondes avant de conclure qu'un combo est fini
  val COMBO_DELAY_MS = 300  // 300ms = 0.3 secondes
  
  // La boucle principale (FONCTIONNEL) avec délai
  def mainLoop(automaton: Automaton, mapping: Map[String, String]): Unit = {
    // Fonction récursive interne (récursion terminale)
    @tailrec
    def loop(
      currentState: State,
      buffer: List[String],  // Buffer pour afficher la séquence
      lastTime: Long = System.currentTimeMillis()  // Temps de la dernière touche
    ): Unit = {
      val now = System.currentTimeMillis()
      val timeSinceLastKey = now - lastTime
      
      // Si on est dans un état final ET qu'il s'est passé assez de temps
      if (automaton.finalStates.contains(currentState) && timeSinceLastKey > COMBO_DELAY_MS) {
        // Afficher la séquence
        println(buffer.mkString(", "))
        println()
        
        // Afficher les mouvements reconnus
        currentState.movements.foreach { movement =>
          println(s"$movement !!")
        }
        println()
        
        // Réinitialiser (récursion avec état initial)
        loop(automaton.initialState, List.empty, now)
      } else {
        // 1. Lire une touche en temps réel (non-bloquant)
        val key = KeyboardReader.readKey()
        
        key match {
          case Some(keyChar) =>
            // 2. Convertir caractère -> String puis -> symbole
            val keyStr = keyChar.toString.toLowerCase
            mapping.get(keyStr) match {
              case Some(symbol) =>
                val newBuffer = buffer :+ symbol
                
                // 3. Chercher la transition
                automaton.transitions.get((currentState, symbol)) match {
                  case Some(newState) =>
                    // Transition trouvée : continuer avec le nouvel état
                    loop(newState, newBuffer, now)
                  case None =>
                    // Pas de transition : rester dans le même état
                    loop(currentState, newBuffer, now)
                }
              case None =>
                // Touche non mappée : ignorer
                loop(currentState, buffer, lastTime)
            }
          case None =>
            // Pas de touche : continuer à attendre (petit délai pour ne pas surcharger le CPU)
            Thread.sleep(10)
            loop(currentState, buffer, lastTime)
        }
      }
    }
    
    // Commencer la récursion depuis l'état initial
    loop(automaton.initialState, List.empty)
  }
}
```

**Action** :
1. ✅ Créer le fichier `src/main/scala/input/MainLoop.scala`
2. ✅ Copier le code ci-dessus
3. ✅ Vérifier que ça compile

---

## 📝 Étape 9 : Tout assembler

### Créer `main.scala`

```scala
import automaton.{Automaton, AutomatonBuilder}
import grammar.GrammarParser
import input.{KeyboardReader, KeyMapping, MainLoop}

@main
def main(args: Array[String]): Unit = {
  // Vérifier les arguments
  args.headOption match {
    case Some(filePath) =>
      // 1. Parser le fichier
      GrammarParser.parseRules(filePath) match {
        case Right(rules) =>
          // 2. Construire l'automate
          val automaton = AutomatonBuilder.buildAutomaton(rules)
          
          // 3. Créer le key mapping
          val mapping = KeyMapping.createKeyMapping(automaton)
          
          // 4. Afficher les mappings
          KeyMapping.displayMappings(mapping)
          println("----------------------")
          println()
          
          // 5. Initialiser le mode temps réel (lecture sans Entrée)
          KeyboardReader.initialize() match {
            case Right(terminalState) =>
              try {
                // 6. Lancer la boucle
                MainLoop.mainLoop(automaton, mapping)
              } finally {
                // IMPORTANT : Toujours restaurer le terminal avant de quitter !
                KeyboardReader.restore(terminalState)
              }
            case Left(error) =>
              println(s"Erreur activation mode temps réel: $error")
              println("Fonctionnement limité")
              sys.exit(1)
          }
          
        case Left(error) =>
          // Erreur de parsing
          println(error)
          sys.exit(1)
      }
    case None =>
      // Pas d'argument
      println("Usage: ./ft_ality <grammar_file.gmr>")
      sys.exit(1)
  }
}
```

**Action** :
1. ✅ Créer le fichier `src/main/scala/main.scala`
2. ✅ Copier le code ci-dessus
3. ✅ Compiler : `sbt compile`
4. ✅ Exécuter : `sbt run grammars/test.gmr`

---

## 📝 Étape 10 : Tests

### Tester avec un fichier simple

1. ✅ Vérifier que `grammars/test.gmr` existe :
   ```
   Punch: [BP]
   Combo: [BP], [FP]
   Kick: [FP]
   ```

2. ✅ Lancer le programme :
   ```bash
   sbt run grammars/test.gmr
   ```

3. ✅ Vérifier que :
   - Les mappings s'affichent
   - Tu peux taper des touches (sans Entrée si mode raw activé)
   - Les mouvements sont reconnus

### Tester avec un fichier plus complexe

Créer `grammars/mk9.gmr` :
```
Claw Slam (Freddy Krueger): [BP]
Knockdown (Sonya): [BP]
Saibot Blast (Noob Saibot): [BP], [FP]
```

Tester :
```bash
sbt run grammars/mk9.gmr
```

---

## ✅ Checklist finale

### Vérification programmation fonctionnelle

Avant de soumettre, vérifier que le code est 100% fonctionnel :

- [ ] **Aucun `var` dans tout le code** (utiliser `grep -r "var" src/` pour vérifier)
- [ ] **Aucun `while` dans tout le code** (utiliser `grep -r "while" src/` pour vérifier)
- [ ] **Toutes les boucles utilisent `@tailrec`** avec récursion terminale
- [ ] **Pas de mutation** : toutes les structures sont immutables (`List`, `Set`, `Map`, case classes)
- [ ] **Pas d'`Array` mutable** : utiliser `List` ou `Vector`
- [ ] **Gestion d'erreurs avec `Either` ou `Option`** : pas d'exceptions pour la logique métier
- [ ] **Fonctions pures** : pas d'effets de bord sauf I/O (lecture fichier, clavier, affichage)

### Vérification fonctionnalités

- [ ] Le projet compile sans erreur (`sbt compile`)
- [ ] Le programme lit correctement les fichiers de grammaire
- [ ] L'automate se construit correctement
- [ ] Les touches sont lues en temps réel (sans Entrée sur Linux/Mac)
- [ ] Les combos courts sont reconnus (ex: `[BP]`)
- [ ] Les combos longs sont reconnus (ex: `[BP], [FP]`)
- [ ] Le délai de 300ms fonctionne pour les combos
- [ ] Les mappings de touches sont affichés
- [ ] Le terminal est restauré correctement (important !)

---

## 🐛 Problèmes courants

### Problème 1 : "Impossible d'activer le mode raw"

**Solution** : Sur Windows, utilise `jline3`. Voir la section Alternative dans `GUIDE_SIMPLE.md`.

### Problème 2 : Le terminal reste cassé après le programme

**Solution** : Assure-toi d'avoir un `finally` qui appelle `KeyboardReader.restore(terminalState)` avec l'état retourné par `initialize()`.

### Problème 3 : Les combos longs ne sont pas reconnus

**Solution** : Vérifie que le délai de 300ms est bien implémenté dans la boucle principale.

---

## 📚 Ressources

- `GUIDE_SIMPLE.md` : Explications détaillées de chaque concept
- `DOCUMENTATION.md` : Documentation complète
- `PLAN.md` : Architecture du projet

---

## 🎯 Prochaines étapes (Bonus)

Une fois que tout fonctionne :

1. Ajouter des tests unitaires
2. Améliorer la gestion d'erreurs
3. Ajouter un mode debug
4. Optimiser les performances

---

**Bon courage ! 🚀**

