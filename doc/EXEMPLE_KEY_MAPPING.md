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

**Étape 1** : Parser le fichier pour extraire les mappings et les règles

```scala
// Fichier grammars/mk9.gmr contient :
// - Mappings de touches (format: "touche -> symbole")
// - Ligne vide
// - Règles de grammaire (format: "nom: séquence")

val (mappings, rules) = GrammarParser.parseRules("grammars/mk9.gmr")
// mappings = Map("q" -> "Block", "d" -> "[BP]", "x" -> "[FP]", ...)
// rules = List(
//   ("Claw Slam (Freddy Krueger)", List("[BP]")),
//   ("Knockdown (Sonya)", List("[BP]")),
//   ...
// )
```

**Étape 2** : Construire l'automate à partir des règles

```scala
val automaton = AutomatonBuilder.buildAutomaton(rules)
```

**⚠️ IMPORTANT** : Les mappings sont **fournis dans le fichier**, pas générés automatiquement !

**Étape 3** : Afficher les mappings (déjà parsés depuis le fichier)

```scala
KeyMapping.displayMappings(mappings)
// Affiche les mappings tels qu'ils sont dans le fichier
```

---

## 🎮 Flux complet avec key mapping

### 1. Démarrage du programme

```scala
@main
def main(args: Array[String]): Unit = {
  // 1. Parser le fichier (mappings + règles)
  GrammarParser.parseRules(args(0)) match {
    case Right((mappings, rules)) =>
      // 2. Construire l'automate à partir des règles
      val automaton = AutomatonBuilder.buildAutomaton(rules)
      
      // 3. Afficher les mappings (déjà parsés depuis le fichier)
      KeyMapping.displayMappings(mappings)
      
      // 4. Séparateur
      println("----------------------")
      println()
      
      // 5. Lancer la boucle de reconnaissance
      MainLoop.runAutomatonLoop(automaton, mappings, automaton.initialState, List.empty)
    case Left(error) =>
      println(s"Erreur: $error")
      sys.exit(1)
  }
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

Claw Slam (Freddy Krueger): [BP]
Knockdown (Sonya): [BP]
Fist of Death (Liu-Kang): [BP]
Saibot Blast (Noob Saibot): [BP], [FP]
Active Duty (Jax): [BP], [FP]
```

**Format** :
- **Première partie** : Mappings de touches (format `touche -> symbole`)
- **Ligne vide** : Séparateur
- **Deuxième partie** : Règles de grammaire (format `nom: séquence`)

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

### Mappings parsés depuis le fichier

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

**⚠️ IMPORTANT** : Les mappings viennent directement du fichier, pas de génération automatique !

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

### 1. Le key mapping vient du fichier

✅ **CORRECT** :
```scala
// Parser les mappings depuis le fichier .gmr
val (mappings, rules) = GrammarParser.parseRules("grammars/mk9.gmr")
// Les mappings sont dans la première partie du fichier
```

### 2. Format du fichier

Le fichier `.gmr` doit avoir ce format :
```
touche -> symbole
touche -> symbole
...

nom: séquence
nom: séquence
```

**Exemple** :
```
d -> [BP]
x -> [FP]

Punch: [BP]
Combo: [BP], [FP]
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

- [ ] Les mappings sont parsés depuis le fichier .gmr (première partie)
- [ ] Le parser sépare correctement les mappings et les règles
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
1. ✅ **Dans le fichier** : Défini dans la première partie du fichier .gmr
2. ✅ **Parsé** : Extrait lors de la lecture du fichier
3. ✅ **Affiché** : Montré à l'utilisateur au démarrage
4. ✅ **Utilisé** : Convertir touches → symboles dans la boucle

