# Documentation complète - ft_ality

## Table des matières

1. [Vue d'ensemble](#vue-densemble)
2. [Concepts théoriques](#concepts-théoriques)
3. [Architecture du système](#architecture-du-système)
4. [Fonctionnement détaillé](#fonctionnement-détaillé)
5. [Algorithmes utilisés](#algorithmes-utilisés)
6. [Flux d'exécution](#flux-dexécution)
7. [Exemples concrets](#exemples-concrets)
8. [Structure des données](#structure-des-données)

---

## Vue d'ensemble

### Qu'est-ce que ft_ality ?

**ft_ality** est un simulateur de mode entraînement de jeu de combat qui utilise un **automate fini** (finite-state automaton) pour reconnaître des combinaisons de touches (combos) en temps réel.

### Principe de base

Le projet fonctionne en deux phases principales :

1. **Phase d'entraînement (Training)** : Construction de l'automate à partir d'un fichier de grammaire contenant les mouvements et leurs séquences de touches
2. **Phase d'exécution (Running)** : Reconnaissance en temps réel des séquences de touches saisies par l'utilisateur

### Exemple simple

```
Fichier de grammaire (mk9.gmr):
  Claw Slam (Freddy Krueger): [BP]
  Saibot Blast (Noob Saibot): [BP], [FP]

Utilisateur tape: [BP]
→ L'automate reconnaît "Claw Slam (Freddy Krueger) !!"

Utilisateur tape: [BP], [FP]
→ L'automate reconnaît "Saibot Blast (Noob Saibot) !!"
```

---

## Concepts théoriques

### 1. Automate fini (Finite-State Automaton)

Un automate fini est un modèle mathématique qui peut être dans un nombre fini d'états et qui change d'état en fonction des symboles qu'il lit.

#### Définition formelle

Un automate fini `A` est un tuple :
```
A = ⟨Q, Σ, Q₀, F, δ⟩
```

Où :
- **Q** : Ensemble fini d'états (Set[State])
- **Σ** (Sigma) : Alphabet d'entrée - tous les symboles possibles (Set[Symbol])
- **Q₀** : État initial (State)
- **F** : Ensemble des états finaux/acceptants (Set[State])
- **δ** (delta) : Fonction de transition `Q × Σ → Q`

#### Comment ça marche ?

1. L'automate commence dans l'état initial `Q₀`
2. Il lit les symboles un par un de gauche à droite
3. Pour chaque symbole, il utilise la fonction de transition `δ` pour passer à un nouvel état
4. Si après avoir lu tous les symboles, l'automate est dans un état final `F`, alors il **reconnaît** la séquence
5. Sinon, la séquence n'est **pas reconnue**

#### Exemple visuel

```
État initial (0) --[BP]--> État 1 --[FP]--> État 2 (FINAL)
                              |
                              | (reconnaît "Claw Slam")
                              |
                              v
                         "Claw Slam !!"
```

### 2. Langages réguliers

Les automates finis reconnaissent les **langages réguliers** (type 3 dans la hiérarchie de Chomsky).

Dans notre cas, les "mots" sont des séquences de touches comme `[BP]`, `[BP], [FP]`, etc.

### 3. Fonction de transition

La fonction `δ` définit comment l'automate passe d'un état à un autre :

```
δ(état_actuel, symbole) → nouvel_état
```

**Exemple** :
- `δ(État0, "[BP]")` → `État1`
- `δ(État1, "[FP]")` → `État2`

---

## Architecture du système

### Structure modulaire

```
┌─────────────────────────────────────────┐
│           main.scala                    │  Point d'entrée
│  (Gestion arguments, orchestration)     │
└──────────────┬──────────────────────────┘
               │
               ├──────────────────────────────────┐
               │                                  │
┌──────────────▼──────────────┐  ┌──────────────▼──────────────┐
│   grammar/                   │  │   automaton/                │
│   - GrammarParser.scala      │  │   - Automaton.scala         │
│   - Grammar.scala            │  │   - AutomatonBuilder.scala │
│   - Token.scala              │  │   - State.scala             │
│                              │  │   - Transition.scala        │
│  Parse fichier .gmr          │  │  Construction & reconnaissance│
│  → Structure Grammar          │  │  → Automate fini            │
└──────────────┬───────────────┘  └──────────────┬──────────────┘
               │                                  │
               └──────────────┬───────────────────┘
                              │
               ┌──────────────▼──────────────┐
               │   input/                    │
               │   - KeyboardHandler.scala    │
               │   - KeyMapping.scala         │
               │                              │
               │  Lecture clavier             │
               │  Mapping touche → symbole    │
               └──────────────┬──────────────┘
                              │
               ┌──────────────▼──────────────┐
               │   output/                   │
               │   - Display.scala           │
               │   - DebugMode.scala          │
               │                              │
               │  Affichage résultats         │
               │  Mode debug                  │
               └─────────────────────────────┘
```

### Modules détaillés

#### 1. Module `grammar/`

**Responsabilité** : Parser les fichiers de grammaire et extraire les règles

**Fichiers** :
- `Grammar.scala` : Structure de données pour la grammaire
- `GrammarParser.scala` : Parsing du fichier texte
- `Token.scala` : Tokenisation des séquences

**Flux** :
```
Fichier .gmr (texte)
    ↓
GrammarParser.parseGrammarFile()
    ↓
List[Rule] où Rule = (moveName: String, sequence: List[Symbol])
    ↓
Grammar(moves: List[Rule])
```

#### 2. Module `automaton/`

**Responsabilité** : Construction et utilisation de l'automate

**Fichiers** :
- `State.scala` : Représentation d'un état
- `Transition.scala` : Représentation d'une transition
- `Automaton.scala` : Structure de l'automate + fonctions de base
- `AutomatonBuilder.scala` : Construction de l'automate à partir de la grammaire

**Flux** :
```
Grammar
    ↓
AutomatonBuilder.buildAutomaton()
    ↓
Automaton (avec tous les états et transitions)
    ↓
Automaton.recognize() pour reconnaître des séquences
```

#### 3. Module `input/`

**Responsabilité** : Gestion des entrées utilisateur

**Fichiers** :
- `KeyboardHandler.scala` : Lecture des touches clavier
- `KeyMapping.scala` : Mapping automatique touche → symbole

**Flux** :
```
Touche clavier (ex: "d")
    ↓
KeyboardHandler.readKeyInput()
    ↓
KeyMapping.mapKeyToSymbol()
    ↓
Symbol (ex: "[BP]")
```

#### 4. Module `output/`

**Responsabilité** : Affichage des résultats

**Fichiers** :
- `Display.scala` : Affichage des mappings et mouvements reconnus
- `DebugMode.scala` : Affichage détaillé des transitions (bonus)

---

## Fonctionnement détaillé

### Phase 1 : Parsing de la grammaire

#### Étape 1.1 : Lecture du fichier

```scala
// Fichier: grammars/mk9.gmr
Claw Slam (Freddy Krueger): [BP]
Knockdown (Sonya): [BP]
Saibot Blast (Noob Saibot): [BP], [FP]
```

Le parser lit le fichier ligne par ligne.

#### Étape 1.2 : Parsing de chaque ligne

Pour chaque ligne, on extrait :
- **Nom du mouvement** : Partie avant le `:`
- **Séquence de touches** : Partie après le `:`

```scala
"Claw Slam (Freddy Krueger): [BP]"
    ↓
moveName = "Claw Slam (Freddy Krueger)"
sequence = "[BP]"
```

#### Étape 1.3 : Tokenisation

La séquence est découpée en tokens (symboles) :

```scala
"[BP], [FP]"
    ↓
tokenize()
    ↓
List("[BP]", "[FP]")
```

**Algorithme de tokenisation** :
1. Chercher les tokens entre `[` et `]`
2. Séparer par les virgules
3. Nettoyer les espaces

#### Résultat

On obtient une structure `Grammar` contenant :
```scala
Grammar(
  moves = List(
    Rule("Claw Slam (Freddy Krueger)", List("[BP]")),
    Rule("Knockdown (Sonya)", List("[BP]")),
    Rule("Saibot Blast (Noob Saibot)", List("[BP]", "[FP]"))
  )
)
```

### Phase 2 : Construction de l'automate

#### Principe

On construit l'automate en ajoutant chaque règle une par une, en créant les états et transitions nécessaires.

#### Algorithme de construction

**Pour chaque règle** (ex: `"Claw Slam": [BP]`) :

1. **Commencer à l'état initial** (État 0)

2. **Pour chaque symbole de la séquence** :
   - Vérifier s'il existe déjà une transition depuis l'état actuel avec ce symbole
   - Si **OUI** : Suivre cette transition (réutiliser l'état existant)
   - Si **NON** : Créer un nouvel état et une nouvelle transition

3. **Marquer le dernier état comme final** et y associer le nom du mouvement

#### Exemple détaillé

**Règle 1** : `"Claw Slam": [BP]`

```
État initial (0)
    ↓
Existe-t-il une transition (0, "[BP]") ? NON
    ↓
Créer État 1
Créer transition: (0, "[BP]") → 1
    ↓
Marquer État 1 comme FINAL
Associer "Claw Slam" à l'État 1
```

**Résultat après règle 1** :
```
État 0 --[BP]--> État 1 (FINAL: "Claw Slam")
```

**Règle 2** : `"Knockdown": [BP]`

```
État initial (0)
    ↓
Existe-t-il une transition (0, "[BP]") ? OUI → État 1
    ↓
Marquer État 1 comme FINAL (déjà final)
Ajouter "Knockdown" aux mouvements de l'État 1
```

**Résultat après règle 2** :
```
État 0 --[BP]--> État 1 (FINAL: "Claw Slam", "Knockdown")
```

**Règle 3** : `"Saibot Blast": [BP], [FP]`

```
État initial (0)
    ↓
Transition (0, "[BP]") existe → État 1
    ↓
État 1
    ↓
Existe-t-il une transition (1, "[FP]") ? NON
    ↓
Créer État 2
Créer transition: (1, "[FP]") → 2
    ↓
Marquer État 2 comme FINAL
Associer "Saibot Blast" à l'État 2
```

**Résultat final** :
```
État 0 --[BP]--> État 1 (FINAL: "Claw Slam", "Knockdown")
                    |
                    | [FP]
                    v
                 État 2 (FINAL: "Saibot Blast")
```

#### Code fonctionnel de construction

```scala
def buildAutomaton(grammar: Grammar): Automaton = {
  // Commencer avec un automate vide (juste l'état initial)
  val emptyAutomaton = createEmptyAutomaton()
  
  // Ajouter chaque règle une par une
  grammar.moves.foldLeft(emptyAutomaton) { (automaton, rule) =>
    addRule(automaton, rule.tokens, rule.moveName)
  }
}

@tailrec
def addRule(
  automaton: Automaton,
  tokens: List[Symbol],
  moveName: String,
  currentState: State = automaton.initialState
): Automaton = {
  tokens match {
    case Nil =>
      // Fin de la séquence : marquer l'état actuel comme final
      val finalState = currentState.copy(
        isFinal = true,
        moves = currentState.moves + moveName
      )
      addState(automaton, finalState)
      
    case symbol :: rest =>
      // Vérifier si une transition existe déjà
      automaton.transitions.get((currentState, symbol)) match {
        case Some(existingState) =>
          // Transition existe : réutiliser l'état
          addRule(automaton, rest, moveName, existingState)
          
        case None =>
          // Créer un nouvel état et transition
          val newState = createNewState(automaton)
          val withState = addState(automaton, newState)
          val withTransition = addTransition(
            withState,
            Transition(currentState, symbol, newState)
          )
          addRule(withTransition, rest, moveName, newState)
      }
  }
}
```

### Phase 3 : Extraction des mappings de touches ⚠️ IMPORTANT

#### Principe

Les mappings doivent être **automatiquement calculés** à partir de l'alphabet de l'automate, **PAS hardcodés**. C'est une exigence critique du projet !

#### Pourquoi c'est important ?

Le key mapping est la **liaison entre les touches du clavier** et les **symboles de l'automate**. Sans lui, l'automate ne peut pas interpréter les touches tapées par l'utilisateur.

#### Algorithme

1. **Extraire l'alphabet** : Tous les symboles uniques utilisés dans les transitions de l'automate
   ```scala
   // Exemple avec l'automate construit
   alphabet = Set("[BP]", "[FP]", "[BK]", "[FK]", 
                  "Block", "Down", "Up", "Left", "Right", 
                  "Flip Stance", "Tag", "Throw")
   ```

2. **Générer les mappings** : Associer chaque symbole à une touche clavier de manière automatique
   ```scala
   mappings = Map(
     "q" -> "Block",
     "down" -> "Down",
     "w" -> "Flip Stance",
     "left" -> "Left",
     "right" -> "Right",
     "e" -> "Tag",
     "a" -> "Throw",
     "up" -> "Up",
     "s" -> "[BK]",
     "d" -> "[BP]",
     "z" -> "[FK]",
     "x" -> "[FP]"
   )
   ```

3. **Afficher les mappings** au démarrage du programme :
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

#### Code fonctionnel

```scala
object KeyMapping {
  /**
   * Extrait tous les symboles uniques de l'automate.
   * Ces symboles forment l'alphabet de l'automate.
   */
  def extractAlphabet(automaton: Automaton): Set[Symbol] = {
    // Extraire tous les symboles des transitions
    automaton.transitions.keys.map(_._2).toSet
  }
  
  /**
   * Génère automatiquement les mappings touche -> symbole.
   * L'ordre des touches est fixe pour garantir la cohérence.
   */
  def generateKeyMappings(alphabet: Set[Symbol]): Map[String, Symbol] = {
    // Liste ordonnée des touches disponibles
    val availableKeys = List(
      "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
      "a", "s", "d", "f", "g", "h", "j", "k", "l",
      "z", "x", "c", "v", "b", "n", "m",
      "up", "down", "left", "right"
    )
    
    // Associer chaque symbole à une touche
    // On prend les premières touches disponibles
    alphabet.zip(availableKeys).toMap.map(_.swap)
  }
  
  /**
   * Affiche les mappings de manière formatée.
   */
  def displayKeyMappings(mappings: Map[String, Symbol]): Unit = {
    println("Key mappings:")
    mappings.toList.sortBy(_._1).foreach { case (key, symbol) =>
      println(s"$key -> $symbol")
    }
  }
}
```

#### Exemple concret avec mk9.gmr

**Fichier de grammaire** (`grammars/mk9.gmr`) :
```
Claw Slam (Freddy Krueger): [BP]
Knockdown (Sonya): [BP]
Fist of Death (Liu-Kang): [BP]
Saibot Blast (Noob Saibot): [BP], [FP]
Active Duty (Jax): [BP], [FP]
```

**Alphabet extrait de l'automate** :
```scala
Set("[BP]", "[FP]")
```

**Mappings générés** :
```scala
Map(
  "d" -> "[BP]",
  "x" -> "[FP]"
)
```

**⚠️ ATTENTION** : Dans cet exemple, l'alphabet ne contient que `[BP]` et `[FP]`. Mais si la grammaire contient aussi des symboles comme `"Block"`, `"Down"`, etc., ils seront automatiquement inclus dans les mappings !

#### Gestion des symboles directionnels

Certains symboles peuvent être des directions brutes (`"down"`, `"up"`, etc.) qui se mappent à elles-mêmes :

```scala
def generateKeyMappings(alphabet: Set[Symbol]): Map[String, Symbol] = {
  val availableKeys = List("q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
                          "a", "s", "d", "f", "g", "h", "j", "k", "l",
                          "z", "x", "c", "v", "b", "n", "m",
                          "up", "down", "left", "right")
  
  // Séparer les symboles directionnels des autres
  val (directionSymbols, otherSymbols) = alphabet.partition { symbol =>
    Set("up", "down", "left", "right").contains(symbol.toLowerCase)
  }
  
  // Mapper les directions à elles-mêmes
  val directionMappings = directionSymbols.map { dir =>
    dir.toLowerCase -> dir
  }.toMap
  
  // Mapper les autres symboles aux touches restantes
  val remainingKeys = availableKeys.filterNot(directionMappings.keys.contains)
  val otherMappings = otherSymbols.zip(remainingKeys).map(_.swap).toMap
  
  directionMappings ++ otherMappings
}
```

#### Utilisation dans la boucle principale

```scala
// Au démarrage
val alphabet = KeyMapping.extractAlphabet(automaton)
val mappings = KeyMapping.generateKeyMappings(alphabet)
KeyMapping.displayKeyMappings(mappings)

println("----------------------")

// Dans la boucle de reconnaissance
readKeyInput() match {
  case Some(key) =>
    mappings.get(key) match {
      case Some(symbol) =>
        // Utiliser le symbole pour suivre les transitions
        processSymbol(automaton, symbol)
      case None =>
        // Touche non mappée, ignorer
    }
  case None => // Pas d'entrée
}
```

#### ⚠️ Règles importantes

1. **JAMAIS hardcoder les mappings** : Ils doivent être calculés automatiquement
2. **L'alphabet vient de l'automate** : Pas du fichier de grammaire directement
3. **Ordre cohérent** : Les mêmes symboles doivent toujours être mappés aux mêmes touches
4. **Affichage obligatoire** : Les mappings doivent être affichés avant la boucle de reconnaissance

### Phase 4 : Reconnaissance en temps réel

#### Principe

L'automate lit les touches une par une et suit les transitions correspondantes.

#### Algorithme de reconnaissance

**Pour une séquence complète** (ex: `List("[BP]", "[FP]")`) :

```scala
@tailrec
def recognize(
  automaton: Automaton,
  currentState: State,
  input: List[Symbol]
): Option[Set[String]] = {
  input match {
    case Nil =>
      // Fin de la séquence
      if (automaton.finalStates.contains(currentState))
        Some(currentState.moves)  // Reconnu !
      else
        None  // Non reconnu
    
    case symbol :: rest =>
      // Chercher la transition
      automaton.transitions.get((currentState, symbol)) match {
        case Some(nextState) =>
          // Transition trouvée : continuer
          recognize(automaton, nextState, rest)
        case None =>
          // Pas de transition : non reconnu
          None
      }
  }
}
```

#### Boucle principale en temps réel

```scala
@tailrec
def runAutomatonLoop(
  automaton: Automaton,
  mappings: Map[String, Symbol],
  currentState: State,
  inputBuffer: List[Symbol]
): Unit = {
  // Lire une touche (non-bloquant)
  readKeyInput() match {
    case Some(key) =>
      // Convertir touche → symbole
      mapKeyToSymbol(key, mappings) match {
        case Some(symbol) =>
          val newBuffer = inputBuffer :+ symbol
          
          // Suivre la transition
          automaton.transitions.get((currentState, symbol)) match {
            case Some(nextState) =>
              if (automaton.finalStates.contains(nextState)) {
                // État final atteint : afficher les mouvements
                displayMoves(nextState.moves)
                // Réinitialiser à l'état initial
                runAutomatonLoop(
                  automaton,
                  mappings,
                  automaton.initialState,
                  List.empty
                )
              } else {
                // Continuer
                runAutomatonLoop(automaton, mappings, nextState, newBuffer)
              }
            case None =>
              // Pas de transition : rester dans le même état
              runAutomatonLoop(automaton, mappings, currentState, newBuffer)
          }
        case None =>
          // Touche non mappée : ignorer
          runAutomatonLoop(automaton, mappings, currentState, inputBuffer)
      }
    case None =>
      // Pas d'entrée : attendre
      runAutomatonLoop(automaton, mappings, currentState, inputBuffer)
  }
}
```

#### Gestion des séquences partielles

L'automate peut reconnaître des séquences à différents moments :

```
Utilisateur tape: [BP]
→ État 1 (FINAL) atteint
→ Affiche: "Claw Slam !!" et "Knockdown !!"
→ Réinitialise à l'état initial

Utilisateur tape: [BP], [FP]
→ État 1 (FINAL) atteint après [BP]
→ Affiche: "Claw Slam !!" et "Knockdown !!"
→ Continue avec [FP]
→ État 2 (FINAL) atteint
→ Affiche: "Saibot Blast !!"
```

---

## Algorithmes utilisés

### 1. Algorithme de construction d'automate

**Type** : Algorithme glouton (greedy)

**Complexité** :
- Temps : O(n × m) où n = nombre de règles, m = longueur moyenne des séquences
- Espace : O(n × m) pour stocker les états et transitions

**Caractéristiques** :
- Réutilise les états existants quand possible
- Crée de nouveaux états seulement si nécessaire
- Optimise l'espace en partageant les préfixes communs

### 2. Algorithme de reconnaissance

**Type** : Parcours de graphe (automate)

**Complexité** :
- Temps : O(m) où m = longueur de la séquence
- Espace : O(1) (récursion terminale)

**Caractéristiques** :
- Récursion terminale pour efficacité
- Recherche en O(1) dans la Map de transitions
- Décision immédiate (reconnu ou non)

### 3. Algorithme de tokenisation

**Type** : Parsing simple (state machine)

**Complexité** :
- Temps : O(n) où n = longueur de la chaîne
- Espace : O(m) où m = nombre de tokens

**Caractéristiques** :
- Parcourt la chaîne une seule fois
- Reconnaît les tokens entre `[` et `]`
- Gère les séparateurs (virgules, espaces)

---

## Flux d'exécution

### Diagramme de flux complet

```
┌─────────────────────────────────────────────────────────────┐
│                    DÉMARRAGE                                 │
│              main(args: Array[String])                       │
└──────────────────────────┬────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│         VÉRIFICATION DES ARGUMENTS                          │
│  - Vérifier qu'un fichier .gmr est fourni                   │
│  - Retourner erreur si manquant                             │
└──────────────────────────┬────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│         PARSING DE LA GRAMMAIRE                              │
│  GrammarParser.parseGrammarFile(path)                        │
│    ↓                                                         │
│  - Lire le fichier ligne par ligne                          │
│  - Parser chaque ligne (moveName: sequence)                 │
│  - Tokeniser les séquences                                  │
│    ↓                                                         │
│  Grammar(moves: List[Rule])                                  │
└──────────────────────────┬────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│         CONSTRUCTION DE L'AUTOMATE                           │
│  AutomatonBuilder.buildAutomaton(grammar)                    │
│    ↓                                                         │
│  - Créer l'état initial                                      │
│  - Pour chaque règle :                                      │
│    * Parcourir les tokens                                    │
│    * Créer états/transitions si nécessaire                  │
│    * Marquer l'état final                                    │
│    ↓                                                         │
│  Automaton(states, alphabet, initialState,                   │
│            finalStates, transitions)                         │
└──────────────────────────┬────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│         EXTRACTION DES MAPPINGS                              │
│  KeyMapping.extractAlphabet(automaton)                       │
│  KeyMapping.generateKeyMappings(alphabet)                   │
│    ↓                                                         │
│  Map[String, Symbol] (ex: "d" -> "[BP]")                    │
└──────────────────────────┬────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│         AFFICHAGE DES MAPPINGS                               │
│  Display.displayKeyMappings(mappings)                       │
│    ↓                                                         │
│  "Key mappings:                                              │
│   d -> [BP]                                                  │
│   x -> [FP]                                                  │
│   ..."                                                       │
└──────────────────────────┬────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│         BOUCLE PRINCIPALE (TEMPS RÉEL)                       │
│  MainLoop.runAutomatonLoop(...)                             │
│    ↓                                                         │
│  [BOUCLE INFINIE]                                           │
│    │                                                         │
│    ├─► Lire touche clavier                                  │
│    │                                                         │
│    ├─► Convertir touche → symbole                           │
│    │                                                         │
│    ├─► Suivre transition dans l'automate                    │
│    │                                                         │
│    ├─► Si état final atteint :                              │
│    │     * Afficher mouvements reconnus                      │
│    │     * Réinitialiser à l'état initial                    │
│    │                                                         │
│    └─► Répéter                                               │
└─────────────────────────────────────────────────────────────┘
```

### Séquence d'exécution détaillée

#### Étape 1 : Initialisation

```scala
@main
def main(args: Array[String]): Unit = {
  args.headOption match {
    case Some(grammarPath) =>
      // Continuer...
    case None =>
      println("Usage: ./ft_ality <grammar_file.gmr>")
      sys.exit(1)
  }
}
```

#### Étape 2 : Parsing

```scala
val grammarResult = GrammarParser.parseGrammarFile(grammarPath)

grammarResult match {
  case Right(grammar) =>
    // Continuer avec la construction
  case Left(error) =>
    println(s"Erreur: $error")
    sys.exit(1)
}
```

#### Étape 3 : Construction

```scala
val automaton = AutomatonBuilder.buildAutomaton(grammar)
```

#### Étape 4 : Mappings

```scala
val alphabet = KeyMapping.extractAlphabet(automaton)
val mappings = KeyMapping.generateKeyMappings(alphabet)
Display.displayKeyMappings(mappings)
```

#### Étape 5 : Boucle principale

```scala
println("----------------------")
MainLoop.runAutomatonLoop(
  automaton,
  mappings,
  automaton.initialState,
  List.empty
)
```

---

## Exemples concrets

### Exemple 0 : Format de sortie complet avec key mapping

**Fichier de grammaire** (`grammars/mk9.gmr`) :
```
Claw Slam (Freddy Krueger): [BP]
Knockdown (Sonya): [BP]
Fist of Death (Liu-Kang): [BP]
Saibot Blast (Noob Saibot): [BP], [FP]
Active Duty (Jax): [BP], [FP]
```

**Exécution** :
```bash
% ./ft_ality grammars/mk9.gmr
```

**Sortie attendue** :
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

----------------------

[BP]

Claw Slam (Freddy Krueger) !!

Knockdown (Sonya) !!

Fist of Death (Liu-Kang) !!

[BP], [FP]

Saibot Blast (Noob Saibot) !!

Active Duty (Jax) !!
```

**Explication** :
1. **Key mappings** : Affichés automatiquement au démarrage
   - Calculés à partir de l'alphabet de l'automate
   - Format: `touche -> symbole`
   - Séparateur: ligne vide puis `----------------------`

2. **Reconnaissance** :
   - Quand l'utilisateur tape `d` (mappé à `[BP]`)
   - L'automate reconnaît et affiche les 3 mouvements associés
   - Quand l'utilisateur tape `d` puis `x` (mappé à `[BP], [FP]`)
   - L'automate reconnaît et affiche les 2 mouvements associés

### Exemple 1 : Grammaire simple

**Fichier `simple.gmr`** :
```
Punch: [BP]
Kick: [FK]
Combo: [BP], [FK]
```

**Construction de l'automate** :

```
État 0 (initial)
    |
    ├─[BP]─► État 1 (FINAL: "Punch")
    |           |
    |           └─[FK]─► État 2 (FINAL: "Combo")
    |
    └─[FK]─► État 3 (FINAL: "Kick")
```

**Reconnaissance** :

| Saisie utilisateur | État final | Résultat |
|---------------------|------------|----------|
| `[BP]` | État 1 | ✅ "Punch !!" |
| `[FK]` | État 3 | ✅ "Kick !!" |
| `[BP], [FK]` | État 2 | ✅ "Combo !!" |
| `[BP], [BP]` | Aucun | ❌ Non reconnu |

### Exemple 2 : Grammaire avec préfixes communs

**Fichier `mk9.gmr`** :
```
Claw Slam: [BP]
Knockdown: [BP]
Saibot Blast: [BP], [FP]
```

**Construction de l'automate** :

```
État 0 (initial)
    |
    └─[BP]─► État 1 (FINAL: "Claw Slam", "Knockdown")
                |
                └─[FP]─► État 2 (FINAL: "Saibot Blast")
```

**Points importants** :
- Les deux premiers mouvements partagent le même état final (État 1)
- Le troisième mouvement réutilise l'État 1 comme état intermédiaire
- Optimisation de l'espace : pas de duplication

**Reconnaissance** :

| Saisie utilisateur | États visités | Résultat |
|---------------------|---------------|----------|
| `[BP]` | 0 → 1 | ✅ "Claw Slam !!"<br>✅ "Knockdown !!" |
| `[BP], [FP]` | 0 → 1 → 2 | ✅ "Saibot Blast !!" |

### Exemple 3 : Reconnaissance en temps réel

**Scénario** : L'utilisateur tape les touches progressivement

```
Temps 0: État = 0, Buffer = []
    ↓
Utilisateur tape "d" (mappé à "[BP]")
    ↓
Temps 1: État = 1, Buffer = ["[BP]"]
    ↓
État 1 est FINAL → Affiche "Claw Slam !!" et "Knockdown !!"
    ↓
Réinitialise: État = 0, Buffer = []
    ↓
Utilisateur tape "x" (mappé à "[FP]")
    ↓
Temps 2: État = 0, Buffer = ["[FP]"]
    ↓
Pas de transition (0, "[FP]") → Reste à l'état 0
    ↓
Utilisateur tape "d" (mappé à "[BP]")
    ↓
Temps 3: État = 1, Buffer = ["[FP]", "[BP]"]
    ↓
État 1 est FINAL → Affiche "Claw Slam !!" et "Knockdown !!"
    ↓
Réinitialise: État = 0, Buffer = []
```

**Note** : L'automate reconnaît `[BP]` même si `[FP]` a été tapé avant, car il réinitialise après chaque reconnaissance.

---

## Structure des données

### Types de base

```scala
type Symbol = String  // Ex: "[BP]", "[FP]", "down"
type StateId = Int    // Identifiant unique d'un état
```

### State

```scala
case class State(
  id: StateId,           // Identifiant unique
  isFinal: Boolean,      // Est-ce un état final ?
  moves: Set[String]     // Noms des mouvements reconnus à cet état
)
```

**Exemple** :
```scala
State(1, isFinal = true, Set("Claw Slam", "Knockdown"))
```

### Transition

```scala
case class Transition(
  from: State,    // État source
  symbol: Symbol, // Symbole déclencheur
  to: State       // État destination
)
```

**Exemple** :
```scala
Transition(State(0, false, Set()), "[BP]", State(1, true, Set("Claw Slam")))
```

### Automaton

```scala
case class Automaton(
  states: Set[State],                              // Tous les états
  alphabet: Set[Symbol],                           // Tous les symboles
  initialState: State,                             // État initial
  finalStates: Set[State],                         // États finaux
  transitions: Map[(State, Symbol), State]        // Fonction de transition
)
```

**Exemple** :
```scala
Automaton(
  states = Set(State(0, false, Set()), State(1, true, Set("Claw Slam"))),
  alphabet = Set("[BP]", "[FP]"),
  initialState = State(0, false, Set()),
  finalStates = Set(State(1, true, Set("Claw Slam"))),
  transitions = Map(
    (State(0, false, Set()), "[BP]") -> State(1, true, Set("Claw Slam"))
  )
)
```

### Grammar

```scala
case class Rule(
  moveName: String,        // Nom du mouvement
  sequence: List[Symbol]    // Séquences de touches
)

case class Grammar(
  moves: List[Rule]         // Liste de toutes les règles
)
```

**Exemple** :
```scala
Grammar(
  moves = List(
    Rule("Claw Slam", List("[BP]")),
    Rule("Saibot Blast", List("[BP]", "[FP]"))
  )
)
```

---

## Résumé

### Comment ça marche en 5 points

1. **Parsing** : Le fichier de grammaire est lu et transformé en structure `Grammar`
2. **Construction** : L'automate est construit en ajoutant chaque règle, créant les états et transitions nécessaires
3. **Mapping** : Les touches clavier sont automatiquement mappées aux symboles de l'automate
4. **Reconnaissance** : L'automate suit les transitions en fonction des touches tapées
5. **Affichage** : Quand un état final est atteint, les mouvements correspondants sont affichés

### Points clés

- ✅ **Automatique** : Les mappings sont calculés automatiquement
- ✅ **Efficace** : Réutilisation des états communs
- ✅ **Temps réel** : Reconnaissance instantanée
- ✅ **Fonctionnel** : Code pur sans mutation
- ✅ **Extensible** : Facile d'ajouter de nouveaux mouvements

---

## Questions fréquentes

### Q: Pourquoi utiliser un automate fini ?

**R:** Les automates finis sont parfaits pour reconnaître des **langages réguliers**, ce qui correspond exactement à notre cas (séquences de touches). Ils sont :
- Efficaces (reconnaissance en O(n))
- Simples à implémenter
- Faciles à comprendre et déboguer

### Q: Que se passe-t-il si deux mouvements ont la même séquence ?

**R:** Ils partagent le même état final. Quand cet état est atteint, **tous les mouvements** sont affichés.

Exemple : `"Punch": [BP]` et `"Jab": [BP]` → Même état final → Affiche les deux.

### Q: Comment l'automate gère-t-il les séquences partielles ?

**R:** L'automate suit les transitions au fur et à mesure. Si une séquence partielle correspond à un état final, elle est reconnue immédiatement. Sinon, l'automate attend la suite.

### Q: Pourquoi réinitialiser après chaque reconnaissance ?

**R:** Pour permettre de reconnaître plusieurs mouvements successifs. Sinon, l'automate resterait bloqué dans un état final.

### Q: L'automate peut-il reconnaître des séquences de longueur variable ?

**R:** Oui ! C'est même l'un de ses avantages. L'automate peut reconnaître à la fois `[BP]` (1 symbole) et `[BP], [FP]` (2 symboles).

---

## Conclusion

Ce projet démontre l'utilisation pratique des automates finis pour la reconnaissance de patterns en temps réel. L'architecture fonctionnelle garantit un code robuste, testable et maintenable, tout en respectant les principes de la programmation fonctionnelle pure.

