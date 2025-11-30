# Exemple concret : Key Mapping avec mk9.gmr

## 📋 Format de sortie attendu

Quand on exécute :
```bash
./ft_ality grammars/mk9.gmr
```

On doit obtenir cette sortie exacte :

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

---

## 🔍 Analyse du key mapping

### Comment les mappings sont générés ?

**Étape 1** : Construire l'automate à partir de la grammaire

```scala
// Fichier grammars/mk9.gmr
val regles = List(
  ("Claw Slam (Freddy Krueger)", List("[BP]")),
  ("Knockdown (Sonya)", List("[BP]")),
  ("Fist of Death (Liu-Kang)", List("[BP]")),
  ("Saibot Blast (Noob Saibot)", List("[BP]", "[FP]")),
  ("Active Duty (Jax)", List("[BP]", "[FP]"))
)

val automate = construireAutomate(regles)
```

**Étape 2** : Extraire l'alphabet de l'automate

```scala
def extractAlphabet(automaton: Automaton): Set[Symbol] = {
  // Parcourir toutes les transitions et extraire les symboles
  automaton.transitions.keys.map(_._2).toSet
}

val alphabet = extractAlphabet(automate)
// Résultat: Set("[BP]", "[FP]")
```

**⚠️ Note** : Si la grammaire contient aussi des symboles comme `"Block"`, `"Down"`, etc., ils seront automatiquement inclus dans l'alphabet !

**Étape 3** : Générer les mappings automatiquement

```scala
def generateKeyMappings(alphabet: Set[Symbol]): Map[String, Symbol] = {
  val availableKeys = List(
    "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
    "a", "s", "d", "f", "g", "h", "j", "k", "l",
    "z", "x", "c", "v", "b", "n", "m",
    "up", "down", "left", "right"
  )
  
  // Si l'alphabet contient des directions, les mapper à elles-mêmes
  val (directions, autres) = alphabet.partition { s =>
    Set("up", "down", "left", "right").contains(s.toLowerCase)
  }
  
  val mappingsDirections = directions.map(d => d.toLowerCase -> d).toMap
  val touchesRestantes = availableKeys.filterNot(mappingsDirections.keys.contains)
  val mappingsAutres = autres.zip(touchesRestantes).map(_.swap).toMap
  
  mappingsDirections ++ mappingsAutres
}

val mappings = generateKeyMappings(alphabet)
// Résultat: Map("d" -> "[BP]", "x" -> "[FP]")
```

**Étape 4** : Afficher les mappings

```scala
def displayKeyMappings(mappings: Map[String, Symbol]): Unit = {
  println("Key mappings:")
  println()  // Ligne vide
  mappings.toList.sortBy(_._1).foreach { case (key, symbol) =>
    println(s"$key -> $symbol")
  }
}

displayKeyMappings(mappings)
```

---

## 🎮 Flux complet avec key mapping

### 1. Démarrage du programme

```scala
@main
def main(args: Array[String]): Unit = {
  // 1. Parser la grammaire
  val grammar = GrammarParser.parseGrammarFile(args(0))
  
  // 2. Construire l'automate
  val automaton = AutomatonBuilder.buildAutomaton(grammar)
  
  // 3. Extraire l'alphabet
  val alphabet = KeyMapping.extractAlphabet(automaton)
  
  // 4. Générer les mappings
  val mappings = KeyMapping.generateKeyMappings(alphabet)
  
  // 5. Afficher les mappings
  KeyMapping.displayKeyMappings(mappings)
  
  // 6. Séparateur
  println("----------------------")
  println()
  
  // 7. Lancer la boucle de reconnaissance
  MainLoop.runAutomatonLoop(automaton, mappings, automaton.initialState, List.empty)
}
```

### 2. Boucle de reconnaissance avec key mapping

```scala
@tailrec
def runAutomatonLoop(
  automaton: Automaton,
  mappings: Map[String, Symbol],
  currentState: State,
  inputBuffer: List[Symbol]
): Unit = {
  // Lire une touche du clavier
  readKeyInput() match {
    case Some(key) =>
      // Convertir touche → symbole via le mapping
      mappings.get(key) match {
        case Some(symbol) =>
          // Symbole trouvé : suivre la transition
          automaton.transitions.get((currentState, symbol)) match {
            case Some(nextState) =>
              if (automaton.finalStates.contains(nextState)) {
                // État final atteint : afficher les mouvements
                displayMoves(nextState.moves)
                // Réinitialiser
                runAutomatonLoop(
                  automaton,
                  mappings,
                  automaton.initialState,
                  List.empty
                )
              } else {
                // Continuer
                runAutomatonLoop(automaton, mappings, nextState, inputBuffer :+ symbol)
              }
            case None =>
              // Pas de transition : rester dans le même état
              runAutomatonLoop(automaton, mappings, currentState, inputBuffer)
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

---

## 📊 Exemple détaillé : mk9.gmr

### Fichier de grammaire

```
Claw Slam (Freddy Krueger): [BP]
Knockdown (Sonya): [BP]
Fist of Death (Liu-Kang): [BP]
Saibot Blast (Noob Saibot): [BP], [FP]
Active Duty (Jax): [BP], [FP]
```

### Automate construit

```
État 0 (initial)
    |
    | [BP]
    v
État 1 (FINAL: "Claw Slam", "Knockdown", "Fist of Death")
    |
    | [FP]
    v
État 2 (FINAL: "Saibot Blast", "Active Duty")
```

### Alphabet extrait

```scala
alphabet = Set("[BP]", "[FP]")
```

### Mappings générés

```scala
mappings = Map(
  "d" -> "[BP]",
  "x" -> "[FP]"
)
```

### Scénario d'utilisation

**Utilisateur tape `d`** :
1. `readKeyInput()` → `Some("d")`
2. `mappings.get("d")` → `Some("[BP]")`
3. Transition `(État 0, "[BP]")` → `État 1`
4. `État 1` est final → Affiche :
   ```
   Claw Slam (Freddy Krueger) !!
   Knockdown (Sonya) !!
   Fist of Death (Liu-Kang) !!
   ```
5. Réinitialise à `État 0`

**Utilisateur tape `d` puis `x`** :
1. `readKeyInput()` → `Some("d")`
2. `mappings.get("d")` → `Some("[BP]")`
3. Transition `(État 0, "[BP]")` → `État 1`
4. `readKeyInput()` → `Some("x")`
5. `mappings.get("x")` → `Some("[FP]")`
6. Transition `(État 1, "[FP]")` → `État 2`
7. `État 2` est final → Affiche :
   ```
   Saibot Blast (Noob Saibot) !!
   Active Duty (Jax) !!
   ```
8. Réinitialise à `État 0`

---

## ⚠️ Points critiques

### 1. Le key mapping doit être automatique

❌ **FAUX** :
```scala
// Hardcodé - INTERDIT !
val mappings = Map(
  "d" -> "[BP]",
  "x" -> "[FP]"
)
```

✅ **CORRECT** :
```scala
// Calculé automatiquement
val alphabet = extractAlphabet(automaton)
val mappings = generateKeyMappings(alphabet)
```

### 2. L'alphabet vient de l'automate, pas du fichier

❌ **FAUX** :
```scala
// Lire directement du fichier - INTERDIT !
val alphabet = lireSymbolesDuFichier(grammarFile)
```

✅ **CORRECT** :
```scala
// Extraire de l'automate construit
val automaton = buildAutomaton(grammar)
val alphabet = extractAlphabet(automaton)
```

### 3. L'affichage doit être exact

Le format doit être :
```
Key mappings:

touche -> symbole
touche -> symbole
...
```

Avec :
- Titre "Key mappings:"
- Ligne vide
- Une ligne par mapping : `touche -> symbole`
- Tri alphabétique des touches (optionnel mais recommandé)

### 4. Le séparateur est obligatoire

Après les mappings, afficher :
```
----------------------
```

Puis une ligne vide avant les reconnaissances.

---

## 🧪 Tests à effectuer

### Test 1 : Vérifier l'extraction de l'alphabet

```scala
val automaton = buildAutomaton(grammar)
val alphabet = extractAlphabet(automaton)
assert(alphabet == Set("[BP]", "[FP]"))
```

### Test 2 : Vérifier la génération des mappings

```scala
val alphabet = Set("[BP]", "[FP]")
val mappings = generateKeyMappings(alphabet)
assert(mappings("d") == "[BP]")
assert(mappings("x") == "[FP]")
```

### Test 3 : Vérifier l'affichage

```scala
val mappings = Map("d" -> "[BP]", "x" -> "[FP]")
displayKeyMappings(mappings)
// Doit afficher exactement :
// Key mappings:
//
// d -> [BP]
// x -> [FP]
```

### Test 4 : Vérifier l'utilisation dans la boucle

```scala
val mappings = Map("d" -> "[BP]")
val key = "d"
val symbol = mappings.get(key)  // Some("[BP]")
// Utiliser symbol dans l'automate
```

---

## 📝 Code complet fonctionnel

```scala
object KeyMapping {
  /**
   * Extrait l'alphabet (tous les symboles uniques) de l'automate.
   */
  def extractAlphabet(automaton: Automaton): Set[Symbol] = {
    automaton.transitions.keys.map(_._2).toSet
  }
  
  /**
   * Génère les mappings touche -> symbole automatiquement.
   */
  def generateKeyMappings(alphabet: Set[Symbol]): Map[String, Symbol] = {
    val availableKeys = List(
      "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
      "a", "s", "d", "f", "g", "h", "j", "k", "l",
      "z", "x", "c", "v", "b", "n", "m",
      "up", "down", "left", "right"
    )
    
    // Séparer directions et autres symboles
    val (directions, autres) = alphabet.partition { s =>
      Set("up", "down", "left", "right").contains(s.toLowerCase)
    }
    
    // Mapper les directions à elles-mêmes
    val mappingsDirections = directions.map(d => d.toLowerCase -> d).toMap
    
    // Mapper les autres symboles aux touches restantes
    val touchesRestantes = availableKeys.filterNot(mappingsDirections.keys.contains)
    val mappingsAutres = autres.zip(touchesRestantes).map(_.swap).toMap
    
    mappingsDirections ++ mappingsAutres
  }
  
  /**
   * Affiche les mappings au format requis.
   */
  def displayKeyMappings(mappings: Map[String, Symbol]): Unit = {
    println("Key mappings:")
    println()  // Ligne vide
    mappings.toList.sortBy(_._1).foreach { case (key, symbol) =>
      println(s"$key -> $symbol")
    }
  }
}
```

---

## ✅ Checklist

Avant de soumettre, vérifier :

- [ ] Les mappings sont calculés automatiquement (pas hardcodés)
- [ ] L'alphabet est extrait de l'automate (pas du fichier)
- [ ] L'affichage respecte le format exact :
  - [ ] Titre "Key mappings:"
  - [ ] Ligne vide
  - [ ] Format `touche -> symbole`
  - [ ] Séparateur `----------------------`
- [ ] Les mappings sont utilisés dans la boucle de reconnaissance
- [ ] Les touches sont converties en symboles avant d'être utilisées dans l'automate
- [ ] Les touches non mappées sont ignorées (pas d'erreur)

---

## 🎯 Résumé

Le **key mapping** est la **liaison essentielle** entre :
- Les **touches du clavier** (ce que l'utilisateur tape)
- Les **symboles de l'automate** (ce que l'automate comprend)

**Sans key mapping, l'automate ne peut pas fonctionner !**

Il doit être :
1. ✅ **Automatique** : Calculé à partir de l'alphabet
2. ✅ **Affiché** : Montré à l'utilisateur au démarrage
3. ✅ **Utilisé** : Convertir touches → symboles dans la boucle

