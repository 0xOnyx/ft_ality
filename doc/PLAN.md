# Plan d'implémentation - ft_ality en Scala

## Vue d'ensemble

Ce projet consiste à implémenter un automate fini (finite-state automaton) pour reconnaître des combos de jeux de combat à partir de séquences de touches clavier.

---

## Architecture du projet

### Structure des modules

```
src/main/scala/
├── main.scala                    # Point d'entrée principal
├── automaton/
│   ├── Automaton.scala          # Définition de l'automate fini
│   ├── State.scala              # Représentation des états
│   └── Transition.scala         # Gestion des transitions
├── grammar/
│   ├── GrammarParser.scala      # Parsing des fichiers de grammaire
│   ├── Grammar.scala            # Structure de données pour la grammaire
│   └── Token.scala              # Tokenisation des règles
├── input/
│   ├── KeyboardHandler.scala    # Gestion des entrées clavier
│   └── KeyMapping.scala         # Mapping des touches
└── output/
    ├── Display.scala            # Affichage des résultats
    └── DebugMode.scala          # Mode debug (bonus)
```

---

## Phase 1 : Définition formelle de l'automate

### 1.1 Modélisation de l'automate fini

**Fichier**: `automaton/Automaton.scala`

**Définition formelle**:
- `A = ⟨Q, Σ, Q₀, F, δ⟩`
  - `Q`: Ensemble des états (Set[State])
  - `Σ`: Alphabet d'entrée (Set[Symbol])
  - `Q₀`: État initial (State)
  - `F`: États de reconnaissance (Set[State])
  - `δ`: Fonction de transition (Map[(State, Symbol), State])

**Types de données**:
```scala
type Symbol = String  // Token de la séquence (ex: "[BP]", "down")
type StateId = Int    // Identifiant unique d'un état

case class State(id: StateId, isFinal: Boolean, moves: Set[String])
case class Transition(from: State, symbol: Symbol, to: State)
case class Automaton(
  states: Set[State],
  alphabet: Set[Symbol],
  initialState: State,
  finalStates: Set[State],
  transitions: Map[(State, Symbol), State]
)
```

**Fonctions principales (TOUTES FONCTIONNELLES)**:
- `createEmptyAutomaton(): Automaton` - Créer un automate vide
  - Fonction pure, retourne un nouvel automate immuable
  
- `addState(automaton: Automaton, state: State): Automaton` - Ajouter un état
  - Fonction pure, retourne un NOUVEAU automate (pas de mutation)
  - Utilise `copy` ou construction immuable
  
- `addTransition(automaton: Automaton, transition: Transition): Automaton` - Ajouter une transition
  - Fonction pure, retourne un NOUVEAU automate
  - Utilise `copy` avec nouvelle Map de transitions
  
- `recognize(automaton: Automaton, input: List[Symbol]): Option[Set[String]]` - Reconnaître une séquence
  - Fonction pure avec récursion terminale
  - Retourne `Option[Set[String]]` pour gérer la non-reconnaissance

---

## Phase 2 : Parsing de la grammaire

### 2.1 Structure des fichiers de grammaire

**Format proposé** (exemple: `grammars/mk9.gmr`):
```
Claw Slam (Freddy Krueger): [BP]
Knockdown (Sonya): [BP]
Fist of Death (Liu-Kang): [BP]
Saibot Blast (Noob Saibot): [BP], [FP]
Active Duty (Jax): [BP], [FP]
```

**Format alternatif** (si besoin):
```
[BP] -> Claw Slam (Freddy Krueger)
[BP] -> Knockdown (Sonya)
[BP], [FP] -> Saibot Blast (Noob Saibot)
```

### 2.2 Parsing et tokenisation

**Fichier**: `grammar/GrammarParser.scala`

**Fonctions (TOUTES FONCTIONNELLES)**:
- `parseGrammarFile(filePath: String): Either[String, Grammar]` - Parser un fichier
  - Retourne `Either` pour gérer les erreurs sans exceptions
  - Fonction pure (lecture fichier isolée dans I/O)
  
- `tokenizeRule(rule: String): List[Symbol]` - Tokeniser une règle
  - Fonction pure, pas d'effet de bord
  - Utilise pattern matching et récursion
  
- `extractMoveName(rule: String): Option[String]` - Extraire le nom du mouvement
  - Retourne `Option` pour gérer les règles invalides
  - Fonction pure

**Gestion d'erreurs**:
- Utiliser `Either[String, T]` pour gérer les erreurs de parsing
- Pas d'exceptions, uniquement des types algébriques

**Fichier**: `grammar/Token.scala`
- Définir les types de tokens
- Parser les séquences avec séparateurs (virgules, espaces)

---

## Phase 3 : Construction de l'automate (Training)

### 3.1 Algorithme de construction

**Fichier**: `automaton/AutomatonBuilder.scala`

**Processus**:
1. Créer l'état initial (id: 0)
2. Pour chaque règle de grammaire:
   - Tokeniser la séquence
   - Parcourir les tokens
   - Créer les états intermédiaires si nécessaire
   - Créer une transition pour chaque token
   - Marquer l'état final avec le nom du mouvement

**Fonctions (TOUTES FONCTIONNELLES)**:
- `buildAutomaton(grammar: Grammar): Automaton` - Construire l'automate complet
  - Utilise `foldLeft` pour itérer sur les règles
  - Fonction pure, pas de mutation
  
- `addRule(automaton: Automaton, tokens: List[Symbol], moveName: String): Automaton` - Ajouter une règle
  - Fonction pure, retourne un nouvel automate
  - Utilise récursion terminale pour parcourir les tokens
  
- `findOrCreateState(automaton: Automaton, stateId: Int): (Automaton, State)` - Gérer les états
  - Retourne un tuple `(nouvel automate, état)` pour gérer la création
  - Fonction pure, pas de mutation

**Optimisations**:
- Réutiliser les états existants quand possible
- Minimiser le nombre d'états (optimisation future)

---

## Phase 4 : Extraction des mappings de touches ⚠️ CRITIQUE

### 4.1 Détection automatique de l'alphabet

**Fichier**: `input/KeyMapping.scala`

**⚠️ IMPORTANT** : Le key mapping est **OBLIGATOIRE** et doit être **automatiquement calculé**, jamais hardcodé !

**Fonctions**:
- `extractAlphabet(automaton: Automaton): Set[Symbol]` - Extraire tous les symboles uniques de l'automate
  - Parcourt toutes les transitions
  - Extrait les symboles (deuxième élément des tuples clés)
  - Retourne un Set pour éliminer les doublons
  
- `generateKeyMappings(alphabet: Set[Symbol]): Map[String, Symbol]` - Générer les mappings automatiquement
  - Prend l'alphabet extrait de l'automate
  - Associe chaque symbole à une touche clavier
  - Gère les symboles directionnels (up, down, left, right) qui se mappent à eux-mêmes
  - Utilise un ordre fixe pour garantir la cohérence
  
- `displayKeyMappings(mappings: Map[String, Symbol]): Unit` - Afficher les mappings
  - Affiche au format: `touche -> symbole`
  - Doit être appelé avant la boucle de reconnaissance

**Mapping automatique**:
- Extraire tous les symboles uniques de l'automate (pas du fichier !)
- Créer un mapping clavier -> symbole de manière automatique
- Exemple de sortie:
  ```
  Key mappings:
  q -> Block
  down -> Down
  w -> Flip Stance
  left -> Left
  right -> Right
  e -> Tag
  a -> Throw
  up -> Up
  s -> [BK]
  d -> [BP]
  z -> [FK]
  x -> [FP]
  ```

**Code fonctionnel**:
```scala
object KeyMapping {
  def extractAlphabet(automaton: Automaton): Set[Symbol] = {
    automaton.transitions.keys.map(_._2).toSet
  }
  
  def generateKeyMappings(alphabet: Set[Symbol]): Map[String, Symbol] = {
    val availableKeys = List("q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
                            "a", "s", "d", "f", "g", "h", "j", "k", "l",
                            "z", "x", "c", "v", "b", "n", "m",
                            "up", "down", "left", "right")
    
    // Gérer les directions qui se mappent à elles-mêmes
    val (directions, others) = alphabet.partition { s =>
      Set("up", "down", "left", "right").contains(s.toLowerCase)
    }
    
    val directionMappings = directions.map(d => d.toLowerCase -> d).toMap
    val remainingKeys = availableKeys.filterNot(directionMappings.keys.contains)
    val otherMappings = others.zip(remainingKeys).map(_.swap).toMap
    
    directionMappings ++ otherMappings
  }
  
  def displayKeyMappings(mappings: Map[String, Symbol]): Unit = {
    println("Key mappings:")
    mappings.toList.sortBy(_._1).foreach { case (key, symbol) =>
      println(s"$key -> $symbol")
    }
  }
}
```

**⚠️ RÈGLES STRICTES**:
- ❌ **JAMAIS hardcoder les mappings** dans le code
- ✅ **Toujours calculer** à partir de l'alphabet de l'automate
- ✅ **Afficher les mappings** avant la boucle de reconnaissance
- ✅ **Utiliser les mappings** pour convertir touches → symboles dans la boucle principale

---

## Phase 5 : Reconnaissance en temps réel

### 5.1 Gestion des entrées clavier

**Fichier**: `input/KeyboardHandler.scala`

**Fonctions (TOUTES FONCTIONNELLES)**:
- `readKeyInput(): Option[String]` - Lire une touche (non-bloquant si possible)
  - Retourne `Option[String]` pour gérer l'absence d'entrée
  - Pas de mutation d'état global
  
- `mapKeyToSymbol(key: String, mappings: Map[String, Symbol]): Option[Symbol]` - Convertir touche -> symbole
  - Fonction pure (pas d'effet de bord)
  - Retourne `Option` si la touche n'est pas mappée
  
- `processInputSequence(automaton: Automaton, symbols: List[Symbol]): Option[Set[String]]` - Traiter une séquence
  - Fonction pure (pas d'effet de bord)
  - Utilise la récursion terminale pour parcourir la séquence

**Boucle principale (FONCTIONNEL PUR)**:
```scala
// Version fonctionnelle avec récursion terminale
@tailrec
def runAutomatonLoop(
  automaton: Automaton,
  mappings: Map[String, Symbol],
  currentState: State,
  inputBuffer: List[Symbol]
): Unit = {
  readKeyInput() match {
    case Some(key) => 
      mapKeyToSymbol(key, mappings) match {
        case Some(symbol) =>
          val newBuffer = inputBuffer :+ symbol
          val nextState = transition(automaton, currentState, symbol)
          val (finalState, updatedBuffer) = 
            if (isFinal(nextState)) {
              displayMoves(nextState.moves)
              (automaton.initialState, List.empty) // Réinitialiser
            } else {
              (nextState, newBuffer)
            }
          runAutomatonLoop(automaton, mappings, finalState, updatedBuffer)
        case None => 
          runAutomatonLoop(automaton, mappings, currentState, inputBuffer)
      }
    case None => 
      runAutomatonLoop(automaton, mappings, currentState, inputBuffer)
  }
}

def runAutomaton(automaton: Automaton, mappings: Map[String, Symbol]): Unit = {
  runAutomatonLoop(automaton, mappings, automaton.initialState, List.empty)
}
```

### 5.2 Affichage des résultats

**Fichier**: `output/Display.scala`

**Fonctions**:
- `displayKeyMappings(mappings: Map[String, Symbol]): Unit`
- `displayRecognizedMoves(moves: Set[String]): Unit`
- `displayInputSequence(sequence: List[Symbol]): Unit`

**Format de sortie**:
```
Key mappings:
q -> Block
down -> Down
...
----------------------
[BP]
Claw Slam (Freddy Krueger) !!
Knockdown (Sonya) !!
```

---

## Phase 6 : Point d'entrée principal

### 6.1 Fonction main

**Fichier**: `main.scala`

**Flux d'exécution**:
1. Lire l'argument de ligne de commande (chemin du fichier de grammaire)
2. Parser le fichier de grammaire
3. Construire l'automate
4. Extraire et afficher les mappings de touches
5. Lancer la boucle de reconnaissance

**Gestion des erreurs**:
- Vérifier les arguments
- Gérer les erreurs de fichier avec `Either`
- Messages d'erreur clairs

---

## Phase 7 : Bonus (optionnel)

### 7.1 Mode Debug

**Fichier**: `output/DebugMode.scala`

**Fonctionnalités**:
- Afficher les transitions d'état en temps réel
- Afficher l'état actuel
- Afficher les états finaux trouvés
- Activable via argument `--debug` ou touche spéciale

### 7.2 Optimisations

- **Récursion terminale**: Toutes les fonctions récursives doivent être tail-recursive
- **Traitement en ligne**: Traiter les entrées au fur et à mesure, pas tout charger en mémoire
- **Minimisation de l'automate**: Réduire le nombre d'états (algorithme de minimisation)

### 7.3 Interface graphique (bonus avancé)

- Utiliser une bibliothèque Scala (ScalaFX, Scala.js, etc.)
- Visualiser l'automate
- Afficher les transitions en temps réel

### 7.4 Support gamepad (bonus avancé)

- Utiliser une bibliothèque Java pour les gamepads
- Mapper les boutons du gamepad aux symboles

---

## Contraintes et bonnes pratiques - PROGRAMMATION FONCTIONNELLE STRICTE

### ⚠️ RÈGLES ABSOLUES - AUCUNE EXCEPTION

#### 1. Immutabilité totale

**INTERDICTIONS STRICTES**:
- ❌ **JAMAIS de `var`** - Utiliser uniquement `val`
- ❌ **JAMAIS de `Array`** - Utiliser `List`, `Vector`, ou `Seq`
- ❌ **JAMAIS de structures mutables** - Pas de `mutable.Map`, `mutable.Set`, etc.
- ❌ **JAMAIS de `ref` ou variables mutables**
- ❌ **JAMAIS de `while` ou `for` avec effets de bord** - Utiliser récursion terminale

**EXEMPLES CORRECTS**:
```scala
// ✅ CORRECT - Immutable
val states: Set[State] = Set(state1, state2)
val transitions: Map[(State, Symbol), State] = Map.empty

// ❌ INTERDIT - Mutable
var currentState = initialState  // INTERDIT
val mutableMap = mutable.Map.empty[String, Int]  // INTERDIT
```

#### 2. Gestion d'erreurs fonctionnelle

**UTILISER UNIQUEMENT**:
- `Option[T]` pour valeurs optionnelles
- `Either[String, T]` pour erreurs avec message
- Types algébriques (sealed traits/enums) pour alternatives

**INTERDICTIONS**:
- ❌ **JAMAIS d'exceptions** (sauf système: `Sys_error` qui ne peut être évité)
- ❌ **JAMAIS de `try/catch`** pour la logique métier
- ❌ **JAMAIS de `null`**

**EXEMPLES CORRECTS**:
```scala
// ✅ CORRECT - Either pour erreurs
def parseGrammarFile(path: String): Either[String, Grammar] = {
  // ... parsing logic
  if (isValid) Right(grammar)
  else Left("Erreur: fichier invalide")
}

// ✅ CORRECT - Option pour valeurs optionnelles
def findState(id: StateId, states: Set[State]): Option[State] = {
  states.find(_.id == id)
}

// ❌ INTERDIT - Exceptions
def parseGrammarFile(path: String): Grammar = {
  if (!isValid) throw new Exception("Erreur")  // INTERDIT
}
```

#### 3. Fonctions pures

**RÈGLES**:
- ✅ Fonctions courtes (< 20 lignes)
- ✅ Définitions imbriquées pour fonctions utilitaires
- ✅ Pas d'effets de bord sauf I/O (lecture clavier, affichage)
- ✅ Fonctions doivent retourner des valeurs, pas modifier l'état

**EXEMPLES CORRECTS**:
```scala
// ✅ CORRECT - Fonction pure
def addTransition(
  automaton: Automaton,
  from: State,
  symbol: Symbol,
  to: State
): Automaton = {
  val newTransitions = automaton.transitions + ((from, symbol) -> to)
  automaton.copy(transitions = newTransitions)
}

// ✅ CORRECT - Fonction avec définition imbriquée
def buildAutomaton(grammar: Grammar): Automaton = {
  def addRule(automaton: Automaton, rule: Rule): Automaton = {
    // Logique de construction
    automaton
  }
  grammar.rules.foldLeft(createEmptyAutomaton())(addRule)
}

// ❌ INTERDIT - Effet de bord dans fonction pure
def addTransition(automaton: Automaton, ...): Automaton = {
  automaton.transitions += ((from, symbol) -> to)  // INTERDIT - mutation
  automaton
}
```

#### 4. Récursion terminale obligatoire

**RÈGLE**: Toutes les boucles doivent utiliser la récursion terminale avec `@tailrec`

**EXEMPLES CORRECTS**:
```scala
// ✅ CORRECT - Récursion terminale
import scala.annotation.tailrec

@tailrec
def recognize(
  automaton: Automaton,
  currentState: State,
  input: List[Symbol]
): Option[Set[String]] = {
  input match {
    case Nil => 
      if (automaton.finalStates.contains(currentState)) 
        Some(currentState.moves)
      else 
        None
    case symbol :: rest =>
      automaton.transitions.get((currentState, symbol)) match {
        case Some(nextState) => 
          recognize(automaton, nextState, rest)
        case None => 
          None
      }
  }
}

// ✅ CORRECT - foldLeft pour itération
def buildAutomaton(grammar: Grammar): Automaton = {
  grammar.rules.foldLeft(createEmptyAutomaton()) { (automaton, rule) =>
    addRule(automaton, rule)
  }
}

// ❌ INTERDIT - Boucle while
def recognize(...): Option[Set[String]] = {
  var state = currentState  // INTERDIT
  var remaining = input     // INTERDIT
  while (remaining.nonEmpty) {  // INTERDIT
    // ...
  }
}
```

#### 5. Structures de données immutables

**UTILISER UNIQUEMENT**:
- `List`, `Vector`, `Seq` pour séquences
- `Set` pour ensembles
- `Map` pour dictionnaires
- `Tuple` pour paires/triplets
- Case classes pour structures de données

**EXEMPLES**:
```scala
// ✅ CORRECT - Structures immutables
val states: Set[State] = Set.empty
val transitions: Map[(State, Symbol), State] = Map.empty
val rules: List[Rule] = List.empty

// ❌ INTERDIT - Structures mutables
import scala.collection.mutable
val mutableSet = mutable.Set.empty[State]  // INTERDIT
val mutableMap = mutable.Map.empty[String, Int]  // INTERDIT
```

#### 6. Pattern matching et types algébriques

**UTILISER**:
- Pattern matching exhaustif
- Sealed traits/enums pour types algébriques
- Case classes pour structures

**EXEMPLES**:
```scala
// ✅ CORRECT - Type algébrique
sealed trait ParseResult
case class Success(grammar: Grammar) extends ParseResult
case class Failure(message: String) extends ParseResult

def parseGrammar(path: String): ParseResult = {
  // ... parsing
  if (isValid) Success(grammar)
  else Failure("Erreur")
}

// ✅ CORRECT - Pattern matching exhaustif
def handleResult(result: ParseResult): Either[String, Grammar] = {
  result match {
    case Success(grammar) => Right(grammar)
    case Failure(msg) => Left(msg)
  }
}
```

#### 7. Composition de fonctions

**UTILISER**:
- `map`, `flatMap`, `filter`, `foldLeft`, `foldRight`
- Composition de fonctions avec `andThen`, `compose`
- Higher-order functions

**EXEMPLES**:
```scala
// ✅ CORRECT - Composition fonctionnelle
def processGrammar(grammar: Grammar): Automaton = {
  grammar.rules
    .map(tokenizeRule)
    .foldLeft(createEmptyAutomaton())(addRule)
}

// ✅ CORRECT - Higher-order functions
def buildAutomaton(grammar: Grammar): Automaton = {
  grammar.rules.foldLeft(createEmptyAutomaton()) { (auto, rule) =>
    addRule(auto, tokenizeRule(rule))
  }
}
```

#### 8. I/O fonctionnel

**RÈGLE**: Isoler les I/O dans des fonctions dédiées, utiliser des types pour représenter les effets

**EXEMPLES**:
```scala
// ✅ CORRECT - I/O isolé
def readKeyInput(): Option[String] = {
  // Lecture clavier (seul endroit avec effet de bord)
  // Retourne Option pour gérer l'absence d'entrée
}

def displayMoves(moves: Set[String]): Unit = {
  // Affichage (effet de bord nécessaire)
  moves.foreach(move => println(s"$move !!"))
}

// ✅ CORRECT - Séparation logique pure / I/O
def processInput(
  automaton: Automaton,
  symbol: Symbol
): Option[Set[String]] = {
  // Logique pure, pas d'I/O
  recognize(automaton, automaton.initialState, List(symbol))
}

def runWithIO(automaton: Automaton): Unit = {
  // I/O ici, logique pure ailleurs
  readKeyInput() match {
    case Some(key) => 
      processInput(automaton, key) match {
        case Some(moves) => displayMoves(moves)
        case None => ()
      }
    case None => ()
  }
}
```

### Structure du code fonctionnel

1. **Modules bien définis**:
   - Séparation claire des responsabilités
   - Pas d'exports inutiles
   - Documentation des fonctions publiques
   - Chaque module exporte uniquement ce qui est nécessaire

2. **Types de données immutables**:
   - Utiliser des case classes pour structures
   - Types algébriques (sealed traits) pour alternatives
   - Type aliases pour clarté
   - Tous les types doivent être immutables

**EXEMPLE DE STRUCTURE MODULAIRE**:
```scala
// automaton/Automaton.scala
package automaton

// Types internes (non exportés)
private type InternalState = Int

// Types exportés
case class State(id: StateId, isFinal: Boolean, moves: Set[String])
case class Automaton(...)

// Fonctions exportées uniquement
object Automaton {
  def createEmpty(): Automaton = ...
  def recognize(automaton: Automaton, input: List[Symbol]): Option[Set[String]] = ...
  // Fonctions utilitaires privées
  private def transition(...): Option[State] = ...
}
```

### Checklist de validation fonctionnelle

Avant de soumettre, vérifier que:
- [ ] **Aucun `var` dans le code** (sauf peut-être dans main pour I/O si nécessaire)
- [ ] **Aucun `Array` ou structure mutable**
- [ ] **Toutes les boucles sont en récursion terminale avec `@tailrec`**
- [ ] **Toutes les erreurs gérées avec `Option` ou `Either`**
- [ ] **Aucune exception levée** (sauf système inévitable)
- [ ] **Toutes les fonctions sont pures** (sauf I/O explicitement marqué)
- [ ] **Pattern matching exhaustif** partout
- [ ] **Composition fonctionnelle** utilisée (`map`, `foldLeft`, etc.)
- [ ] **Types immutables** uniquement

---

## Ordre d'implémentation recommandé

### Étape 1 : Fondations
1. ✅ Définir les types de base (`State`, `Transition`, `Automaton`)
2. ✅ Implémenter les fonctions de base de l'automate
3. ✅ Tests unitaires pour l'automate

### Étape 2 : Parsing
4. ✅ Parser de grammaire simple
5. ✅ Tokenisation des règles
6. ✅ Tests avec fichiers exemple

### Étape 3 : Construction
7. ✅ Builder d'automate
8. ✅ Construction à partir de grammaire
9. ✅ Tests de construction

### Étape 4 : Reconnaissance
10. ✅ Fonction de reconnaissance
11. ✅ Gestion des entrées clavier
12. ✅ Affichage des résultats

### Étape 5 : Intégration
13. ✅ Point d'entrée principal
14. ✅ Gestion des arguments
15. ✅ Tests d'intégration

### Étape 6 : Bonus (si temps)
16. Mode debug
17. Optimisations
18. Interface graphique (optionnel)

---

## Fichiers de test

Créer des fichiers de grammaire d'exemple dans `grammars/`:
- `mk9.gmr` - Exemple du sujet
- `simple.gmr` - Grammaire simple pour tests
- `complex.gmr` - Grammaire complexe pour tests

---

## Makefile

Créer un `Makefile` pour compiler et exécuter:
```makefile
build:
	sbt compile

run:
	sbt run

clean:
	sbt clean

test:
	sbt test
```

---

## Checklist de validation

### Partie obligatoire
- [ ] Automate fini correctement implémenté
- [ ] Parsing des fichiers de grammaire
- [ ] Construction automatique de l'automate
- [ ] Extraction automatique des mappings de touches
- [ ] Reconnaissance en temps réel des combos
- [ ] Affichage correct des résultats
- [ ] Gestion des erreurs sans exceptions (Option/Either uniquement)
- [ ] **Code fonctionnel pur STRICT**:
  - [ ] Aucun `var` dans le code
  - [ ] Aucun `Array` ou structure mutable
  - [ ] Toutes les boucles en récursion terminale avec `@tailrec`
  - [ ] Toutes les fonctions sont pures (sauf I/O explicitement marqué)
  - [ ] Pattern matching exhaustif
  - [ ] Composition fonctionnelle (map, foldLeft, etc.)
- [ ] Makefile fonctionnel

### Bonus
- [ ] Mode debug
- [ ] Récursion terminale partout
- [ ] Traitement en ligne (pas de chargement complet)
- [ ] Code élégant et modulaire
- [ ] Interface graphique (optionnel)
- [ ] Support gamepad (optionnel)

---

## Notes importantes

1. **Pas de `;;` dans le code** (compilation)
2. **Modules autorisés**: Seulement ceux listés dans le sujet (équivalents Scala)
3. **Bibliothèques interdites**: Pas de bibliothèques qui font le travail à notre place
4. **Fichiers de grammaire**: À fournir avec le projet
5. **Style**: Pas imposé, mais code propre et modulaire

---

## Exemples de code fonctionnel pur

### Exemple 1 : Automate avec récursion terminale

```scala
import scala.annotation.tailrec

object Automaton {
  def recognize(
    automaton: Automaton,
    input: List[Symbol]
  ): Option[Set[String]] = {
    @tailrec
    def recognizeLoop(
      currentState: State,
      remaining: List[Symbol]
    ): Option[Set[String]] = {
      remaining match {
        case Nil =>
          if (automaton.finalStates.contains(currentState))
            Some(currentState.moves)
          else
            None
        case symbol :: rest =>
          automaton.transitions.get((currentState, symbol)) match {
            case Some(nextState) => recognizeLoop(nextState, rest)
            case None => None
          }
      }
    }
    recognizeLoop(automaton.initialState, input)
  }
}
```

### Exemple 2 : Construction d'automate avec foldLeft

```scala
object AutomatonBuilder {
  def buildAutomaton(grammar: Grammar): Automaton = {
    def addRule(automaton: Automaton, rule: Rule): Automaton = {
      val tokens = tokenizeRule(rule.sequence)
      addRuleToAutomaton(automaton, tokens, rule.moveName)
    }
    
    grammar.rules.foldLeft(createEmptyAutomaton())(addRule)
  }
  
  @tailrec
  private def addRuleToAutomaton(
    automaton: Automaton,
    tokens: List[Symbol],
    moveName: String,
    currentState: State = automaton.initialState
  ): Automaton = {
    tokens match {
      case Nil =>
        val finalState = currentState.copy(
          isFinal = true,
          moves = currentState.moves + moveName
        )
        addState(automaton, finalState)
      case symbol :: rest =>
        automaton.transitions.get((currentState, symbol)) match {
          case Some(nextState) =>
            addRuleToAutomaton(automaton, rest, moveName, nextState)
          case None =>
            val newState = createNewState(automaton)
            val withState = addState(automaton, newState)
            val withTransition = addTransition(
              withState,
              Transition(currentState, symbol, newState)
            )
            addRuleToAutomaton(withTransition, rest, moveName, newState)
        }
    }
  }
}
```

### Exemple 3 : Parsing avec Either

```scala
object GrammarParser {
  def parseGrammarFile(path: String): Either[String, Grammar] = {
    for {
      lines <- readFileLines(path)
      rules <- parseLines(lines)
    } yield Grammar(rules)
  }
  
  private def parseLines(
    lines: List[String]
  ): Either[String, List[Rule]] = {
    lines
      .zipWithIndex
      .map { case (line, idx) =>
        parseLine(line).left.map(err => s"Ligne ${idx + 1}: $err")
      }
      .foldLeft(Right(List.empty[Rule]): Either[String, List[Rule]]) {
        case (acc, Right(rule)) => acc.map(_ :+ rule)
        case (_, Left(err)) => Left(err)
        case (Left(err), _) => Left(err)
      }
  }
  
  private def parseLine(line: String): Either[String, Rule] = {
    line.split(":").toList match {
      case moveName :: sequence :: Nil =>
        Right(Rule(moveName.trim, tokenizeSequence(sequence.trim)))
      case _ =>
        Left(s"Format invalide: $line")
    }
  }
}
```

### Exemple 4 : Tokenisation fonctionnelle

```scala
object Tokenizer {
  def tokenizeSequence(sequence: String): List[Symbol] = {
    @tailrec
    def tokenizeLoop(
      remaining: String,
      acc: List[Symbol]
    ): List[Symbol] = {
      if (remaining.isEmpty) acc.reverse
      else {
        val (token, rest) = extractNextToken(remaining)
        tokenizeLoop(rest, token :: acc)
      }
    }
    tokenizeLoop(sequence.trim, List.empty)
  }
  
  private def extractNextToken(input: String): (Symbol, String) = {
    input.trim match {
      case s if s.startsWith("[") && s.contains("]") =>
        val endIdx = s.indexOf("]") + 1
        (s.substring(0, endIdx), s.substring(endIdx).trim)
      case s =>
        val parts = s.split(",", 2)
        if (parts.length == 2) {
          (parts(0).trim, parts(1).trim)
        } else {
          (s.trim, "")
        }
    }
  }
}
```

### Exemple 5 : Boucle principale fonctionnelle

```scala
object MainLoop {
  @tailrec
  def runAutomatonLoop(
    automaton: Automaton,
    mappings: Map[String, Symbol],
    currentState: State,
    inputBuffer: List[Symbol]
  ): Unit = {
    readKeyInput() match {
      case Some(key) =>
        mapKeyToSymbol(key, mappings) match {
          case Some(symbol) =>
            val newBuffer = inputBuffer :+ symbol
            automaton.transitions.get((currentState, symbol)) match {
              case Some(nextState) =>
                if (automaton.finalStates.contains(nextState)) {
                  displayMoves(nextState.moves)
                  runAutomatonLoop(
                    automaton,
                    mappings,
                    automaton.initialState,
                    List.empty
                  )
                } else {
                  runAutomatonLoop(automaton, mappings, nextState, newBuffer)
                }
              case None =>
                runAutomatonLoop(automaton, mappings, currentState, newBuffer)
            }
          case None =>
            runAutomatonLoop(automaton, mappings, currentState, inputBuffer)
        }
      case None =>
        runAutomatonLoop(automaton, mappings, currentState, inputBuffer)
    }
  }
}
```

### Exemple 6 : Extraction d'alphabet fonctionnelle

```scala
object KeyMapping {
  def extractAlphabet(automaton: Automaton): Set[Symbol] = {
    automaton.transitions.keys.map(_._2).toSet
  }
  
  def generateKeyMappings(
    alphabet: Set[Symbol]
  ): Map[String, Symbol] = {
    val keyList = List("q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
                       "a", "s", "d", "f", "g", "h", "j", "k", "l",
                       "z", "x", "c", "v", "b", "n", "m",
                       "up", "down", "left", "right")
    
    alphabet.zip(keyList).toMap.map(_.swap)
  }
}
```

---

## Ressources

- Documentation Scala 3: https://docs.scala-lang.org/
- Automates finis: https://fr.wikipedia.org/wiki/Automate_fini
- Langages réguliers: https://fr.wikipedia.org/wiki/Langage_régulier
- Programmation fonctionnelle en Scala: https://www.scala-lang.org/api/current/scala/collection/immutable/index.html

