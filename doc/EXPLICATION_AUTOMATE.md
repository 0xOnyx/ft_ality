# Qu'est-ce qu'un automate fini ? Explication simple avec exemples

## 🎯 Concept de base : Un automate, c'est comme un GPS

Imagine que tu es dans une ville avec plusieurs rues. Tu commences à un point de départ, et tu veux arriver à certains endroits spécifiques (les destinations).

- **Les rues** = les symboles (ex: "[BP]", "[FP]")
- **Les intersections** = les états
- **Le point de départ** = l'état initial
- **Les destinations** = les états finaux (où tu veux arriver)

Un automate, c'est un "GPS" qui te dit : "Si tu es à l'intersection X et que tu prends la rue Y, tu arrives à l'intersection Z".

---

## 📝 Exemple 1 : Automate simple - Reconnaître "oui"

### Le problème

On veut créer un automate qui reconnaît le mot "oui".

### La solution

```scala
// Les symboles (lettres)
val symboles = Set("o", "u", "i")

// Les états
case class Etat(id: Int, estFinal: Boolean)

val etat0 = Etat(0, false)  // État initial
val etat1 = Etat(1, false)   // Après avoir lu "o"
val etat2 = Etat(2, false)   // Après avoir lu "ou"
val etat3 = Etat(3, true)    // Après avoir lu "oui" → FINAL ✅

// Les transitions (les "rues")
val transitions = Map(
  (etat0, "o") -> etat1,  // De l'état 0, avec "o", on va à l'état 1
  (etat1, "u") -> etat2,  // De l'état 1, avec "u", on va à l'état 2
  (etat2, "i") -> etat3   // De l'état 2, avec "i", on va à l'état 3 (FINAL)
)

// L'automate
case class Automate(
  etatInitial: Etat,
  etatsFinaux: Set[Etat],
  transitions: Map[(Etat, String), Etat]
)

val automate = Automate(
  etatInitial = etat0,
  etatsFinaux = Set(etat3),
  transitions = transitions
)
```

### Comment ça marche ?

```scala
// Fonction pour reconnaître un mot
@tailrec
def reconnaitre(
  automate: Automate,
  etatActuel: Etat,
  mot: List[String]  // Le mot à reconnaître, lettre par lettre
): Boolean = {
  mot match {
    case Nil =>
      // On a fini de lire le mot
      // Est-on dans un état final ?
      automate.etatsFinaux.contains(etatActuel)
      
    case lettre :: reste =>
      // On lit une lettre
      // Chercher la transition depuis l'état actuel avec cette lettre
      automate.transitions.get((etatActuel, lettre)) match {
        case Some(nouvelEtat) =>
          // Transition trouvée ! On continue avec le reste du mot
          reconnaitre(automate, nouvelEtat, reste)
        case None =>
          // Pas de transition → le mot n'est pas reconnu
          false
      }
  }
}
```

### Testons !

```scala
// Test 1 : "oui"
val mot1 = List("o", "u", "i")
val resultat1 = reconnaitre(automate, automate.etatInitial, mot1)
println(resultat1)  // true ✅

// Test 2 : "ou"
val mot2 = List("o", "u")
val resultat2 = reconnaitre(automate, automate.etatInitial, mot2)
println(resultat2)  // false ❌ (pas d'état final après "ou")

// Test 3 : "non"
val mot3 = List("n", "o", "n")
val resultat3 = reconnaitre(automate, automate.etatInitial, mot3)
println(resultat3)  // false ❌ (pas de transition pour "n")
```

### Visualisation

```
État 0 (départ)
    |
    | "o"
    v
État 1
    |
    | "u"
    v
État 2
    |
    | "i"
    v
État 3 (FINAL) ✅ → "oui" est reconnu !
```

---

## 🎮 Exemple 2 : Automate pour reconnaître des combos de jeu

### Le problème

On veut reconnaître ces combos :
- `[BP]` → "Punch"
- `[BP], [FP]` → "Combo"

**⚠️ Important** : Il faut aussi créer le **key mapping** qui lie les touches du clavier aux symboles !

### La solution

```scala
// Les symboles (touches du jeu)
type Symbole = String

// Les états
case class Etat(id: Int, estFinal: Boolean, mouvements: Set[String])

val etat0 = Etat(0, false, Set())           // État initial
val etat1 = Etat(1, true, Set("Punch"))      // Après [BP] → FINAL ✅
val etat2 = Etat(2, true, Set("Combo"))      // Après [BP], [FP] → FINAL ✅

// Les transitions
val transitions = Map(
  (etat0, "[BP]") -> etat1,   // De 0, avec [BP], on va à 1
  (etat1, "[FP]") -> etat2    // De 1, avec [FP], on va à 2
)

val automate = Automate(
  etatInitial = etat0,
  etatsFinaux = Set(etat1, etat2),
  transitions = transitions
)
```

### Fonction de reconnaissance améliorée

```scala
def reconnaitreCombo(
  automate: Automate,
  sequence: List[Symbole]
): Option[Set[String]] = {  // Retourne les mouvements reconnus
  @tailrec
  def reconnaitreLoop(
    etatActuel: Etat,
    reste: List[Symbole]
  ): Option[Set[String]] = {
    reste match {
      case Nil =>
        // Fin de la séquence
        if (automate.etatsFinaux.contains(etatActuel))
          Some(etatActuel.mouvements)  // ✅ Reconnu !
        else
          None  // ❌ Non reconnu
          
      case symbole :: suite =>
        // Chercher la transition
        automate.transitions.get((etatActuel, symbole)) match {
          case Some(nouvelEtat) =>
            reconnaitreLoop(nouvelEtat, suite)
          case None =>
            None  // Pas de transition → non reconnu
        }
    }
  }
  
  reconnaitreLoop(automate.etatInitial, sequence)
}
```

### Key Mapping (mapping des touches)

**⚠️ CRUCIAL** : Pour que l'automate fonctionne avec le clavier, il faut créer un mapping !

```scala
// Extraire l'alphabet de l'automate
val alphabet = Set("[BP]", "[FP]")

// Générer les mappings automatiquement
val mappings = Map(
  "d" -> "[BP]",  // La touche "d" correspond au symbole "[BP]"
  "x" -> "[FP]"   // La touche "x" correspond au symbole "[FP]"
)

// Afficher les mappings
println("Key mappings:")
println("d -> [BP]")
println("x -> [FP]")
```

**Utilisation** :
```scala
// L'utilisateur tape "d"
val touche = "d"

// Convertir touche → symbole
val symbole = mappings.get(touche)  // Some("[BP]")

// Utiliser le symbole dans l'automate
symbole match {
  case Some(s) => reconnaitreCombo(automate, List(s))
  case None => None  // Touche non mappée
}
```

### Testons !

```scala
// Test 1 : [BP]
val sequence1 = List("[BP]")
val resultat1 = reconnaitreCombo(automate, sequence1)
println(resultat1)  // Some(Set("Punch")) ✅

// Test 2 : [BP], [FP]
val sequence2 = List("[BP]", "[FP]")
val resultat2 = reconnaitreCombo(automate, sequence2)
println(resultat2)  // Some(Set("Combo")) ✅

// Test 3 : [FP] seul
val sequence3 = List("[FP]")
val resultat3 = reconnaitreCombo(automate, sequence3)
println(resultat3)  // None ❌ (pas de transition depuis l'état initial)

// Test 4 : Avec key mapping (simulation utilisateur)
val toucheUtilisateur = "d"  // Utilisateur tape "d"
val symbole = mappings.get(toucheUtilisateur)  // Some("[BP]")
val resultat4 = symbole.flatMap(s => reconnaitreCombo(automate, List(s)))
println(resultat4)  // Some(Set("Punch")) ✅
```

### Visualisation

```
État 0 (départ)
    |
    | [BP]
    v
État 1 (FINAL) ✅ → "Punch" reconnu !
    |
    | [FP]
    v
État 2 (FINAL) ✅ → "Combo" reconnu !
```

---

## 🔄 Exemple 3 : Automate avec états partagés

### Le problème

On veut reconnaître :
- `[BP]` → "Punch"
- `[BP]` → "Jab" (même séquence, mouvement différent !)
- `[BP], [FP]` → "Combo"

### La solution intelligente

**Astuce** : Les deux premiers mouvements partagent le même état final !

```scala
val etat0 = Etat(0, false, Set())
val etat1 = Etat(1, true, Set("Punch", "Jab"))  // Les deux mouvements ici !
val etat2 = Etat(2, true, Set("Combo"))

val transitions = Map(
  (etat0, "[BP]") -> etat1,
  (etat1, "[FP]") -> etat2
)
```

### Pourquoi c'est intelligent ?

Au lieu de créer deux états séparés pour "Punch" et "Jab", on utilise **un seul état** qui contient les deux mouvements. C'est plus efficace !

### Testons !

```scala
val sequence = List("[BP]")
val resultat = reconnaitreCombo(automate, sequence)
println(resultat)  // Some(Set("Punch", "Jab")) ✅
// Les deux mouvements sont reconnus !
```

### Visualisation

```
État 0 (départ)
    |
    | [BP]
    v
État 1 (FINAL) ✅ → "Punch" ET "Jab" reconnus !
    |
    | [FP]
    v
État 2 (FINAL) ✅ → "Combo" reconnu !
```

---

## 🏗️ Exemple 4 : Comment construire un automate automatiquement ?

### Le problème

On a une liste de règles :
```scala
val regles = List(
  ("Punch", List("[BP]")),
  ("Jab", List("[BP]")),
  ("Combo", List("[BP]", "[FP]"))
)
```

On veut construire l'automate automatiquement.

### La solution

```scala
def construireAutomate(regles: List[(String, List[Symbole])]): Automate = {
  // Commencer avec un automate vide (juste l'état initial)
  var etatCourant = 0
  var etats = Set(Etat(0, false, Set()))
  var transitions = Map.empty[(Etat, Symbole), Etat]
  var etatsFinaux = Set.empty[Etat]
  
  // Pour chaque règle
  for ((mouvement, sequence) <- regles) {
    var etatActuel = Etat(0, false, Set())
    
    // Parcourir chaque symbole de la séquence
    for (symbole <- sequence) {
      // Vérifier si une transition existe déjà
      transitions.get((etatActuel, symbole)) match {
        case Some(etatExistant) =>
          // Transition existe : réutiliser l'état
          etatActuel = etatExistant
        case None =>
          // Pas de transition : créer un nouvel état
          etatCourant += 1
          val nouvelEtat = Etat(etatCourant, false, Set())
          etats = etats + nouvelEtat
          transitions = transitions + ((etatActuel, symbole) -> nouvelEtat)
          etatActuel = nouvelEtat
      }
    }
    
    // Marquer le dernier état comme final et y ajouter le mouvement
    val etatFinal = etatActuel.copy(
      estFinal = true,
      mouvements = etatActuel.mouvements + mouvement
    )
    etats = etats - etatActuel + etatFinal
    etatsFinaux = etatsFinaux + etatFinal
  }
  
  Automate(
    etatInitial = Etat(0, false, Set()),
    etatsFinaux = etatsFinaux,
    transitions = transitions
  )
}
```

### Version fonctionnelle pure (sans var)

```scala
def construireAutomate(regles: List[(String, List[Symbole])]): Automate = {
  // Structure pour garder l'état de construction
  case class Construction(
    etatCourant: Int,
    etats: Set[Etat],
    transitions: Map[(Etat, Symbole), Etat],
    etatsFinaux: Set[Etat]
  )
  
  def ajouterRegle(
    construction: Construction,
    mouvement: String,
    sequence: List[Symbole]
  ): Construction = {
    @tailrec
    def ajouterSequence(
      const: Construction,
      etatActuel: Etat,
      reste: List[Symbole]
    ): (Construction, Etat) = {
      reste match {
        case Nil =>
          // Fin de la séquence : marquer comme final
          val etatFinal = etatActuel.copy(
            estFinal = true,
            mouvements = etatActuel.mouvements + mouvement
          )
          val nouvelleConst = const.copy(
            etats = const.etats - etatActuel + etatFinal,
            etatsFinaux = const.etatsFinaux + etatFinal
          )
          (nouvelleConst, etatFinal)
          
        case symbole :: suite =>
          // Vérifier si transition existe
          const.transitions.get((etatActuel, symbole)) match {
            case Some(etatExistant) =>
              // Réutiliser l'état existant
              ajouterSequence(const, etatExistant, suite)
            case None =>
              // Créer nouvel état et transition
              val nouvelEtat = Etat(const.etatCourant + 1, false, Set())
              val nouvelleConst = const.copy(
                etatCourant = const.etatCourant + 1,
                etats = const.etats + nouvelEtat,
                transitions = const.transitions + ((etatActuel, symbole) -> nouvelEtat)
              )
              ajouterSequence(nouvelleConst, nouvelEtat, suite)
          }
      }
    }
    
    val (nouvelleConst, _) = ajouterSequence(
      construction,
      Etat(0, false, Set()),
      sequence
    )
    nouvelleConst
  }
  
  val constructionInitiale = Construction(
    etatCourant = 0,
    etats = Set(Etat(0, false, Set())),
    transitions = Map.empty,
    etatsFinaux = Set.empty
  )
  
  val constructionFinale = regles.foldLeft(constructionInitiale) {
    case (const, (mouvement, sequence)) =>
      ajouterRegle(const, mouvement, sequence)
  }
  
  Automate(
    etatInitial = Etat(0, false, Set()),
    etatsFinaux = constructionFinale.etatsFinaux,
    transitions = constructionFinale.transitions
  )
}
```

### Testons !

```scala
val regles = List(
  ("Punch", List("[BP]")),
  ("Jab", List("[BP]")),
  ("Combo", List("[BP]", "[FP]"))
)

val automate = construireAutomate(regles)

// Test
val resultat1 = reconnaitreCombo(automate, List("[BP]"))
println(resultat1)  // Some(Set("Punch", "Jab")) ✅

val resultat2 = reconnaitreCombo(automate, List("[BP]", "[FP]"))
println(resultat2)  // Some(Set("Combo")) ✅
```

---

## 🎯 Résumé : Qu'est-ce qu'un automate ?

### En une phrase

**Un automate fini est une machine qui lit des symboles un par un, change d'état selon les transitions, et dit "oui" si elle finit dans un état final.**

### Les 5 composants

1. **États (Q)** : Les "positions" possibles de l'automate
2. **Alphabet (Σ)** : Les symboles qu'on peut lire
3. **État initial (Q₀)** : L'état de départ
4. **États finaux (F)** : Les états où on veut arriver (reconnaissance)
5. **Transitions (δ)** : Les règles "si je suis à l'état X et je lis Y, je vais à l'état Z"

### Analogie simple

C'est comme un **labyrinthe** :
- Tu commences à l'entrée (état initial)
- Tu suis les flèches (transitions) selon les symboles que tu lis
- Si tu arrives à une sortie (état final), tu as gagné ! ✅
- Sinon, tu es perdu ❌

### Exemple visuel complet

```
Automate pour reconnaître "[BP]", "[FP]"

        [BP]
État 0 ──────► État 1 (FINAL: "Punch") ✅
        │
        │ [FP]
        ▼
        État 2 (FINAL: "Combo") ✅
```

**Scénario 1** : On lit `[BP]`
- Départ : État 0
- Transition `(0, "[BP]")` → État 1
- État 1 est final → ✅ "Punch" reconnu !

**Scénario 2** : On lit `[BP], [FP]`
- Départ : État 0
- Transition `(0, "[BP]")` → État 1
- Transition `(1, "[FP]")` → État 2
- État 2 est final → ✅ "Combo" reconnu !

**Scénario 3** : On lit `[FP]`
- Départ : État 0
- Pas de transition `(0, "[FP]")` → ❌ Non reconnu

---

## 💡 Pourquoi utiliser un automate ?

### Avantages

1. **Rapide** : Reconnaissance en O(n) où n = longueur de la séquence
2. **Simple** : Facile à comprendre et implémenter
3. **Efficace** : Partage des états communs (optimisation mémoire)
4. **Déterministe** : Pour chaque état et symbole, un seul état suivant

### Cas d'usage

- ✅ Reconnaissance de mots/clés
- ✅ Validation de formats (emails, téléphones)
- ✅ Parsing simple
- ✅ **Reconnaissance de combos de jeu** (notre cas !)

---

## 🚀 Code complet fonctionnel

Voici un exemple complet et fonctionnel :

```scala
import scala.annotation.tailrec

// Types
type Symbole = String

case class Etat(id: Int, estFinal: Boolean, mouvements: Set[String])

case class Automate(
  etatInitial: Etat,
  etatsFinaux: Set[Etat],
  transitions: Map[(Etat, Symbole), Etat]
)

// Fonction de reconnaissance
def reconnaitre(
  automate: Automate,
  sequence: List[Symbole]
): Option[Set[String]] = {
  @tailrec
  def reconnaitreLoop(
    etatActuel: Etat,
    reste: List[Symbole]
  ): Option[Set[String]] = {
    reste match {
      case Nil =>
        if (automate.etatsFinaux.contains(etatActuel))
          Some(etatActuel.mouvements)
        else
          None
      case symbole :: suite =>
        automate.transitions.get((etatActuel, symbole)) match {
          case Some(nouvelEtat) => reconnaitreLoop(nouvelEtat, suite)
          case None => None
        }
    }
  }
  reconnaitreLoop(automate.etatInitial, sequence)
}

// Exemple d'utilisation
val etat0 = Etat(0, false, Set())
val etat1 = Etat(1, true, Set("Punch"))
val etat2 = Etat(2, true, Set("Combo"))

val automate = Automate(
  etatInitial = etat0,
  etatsFinaux = Set(etat1, etat2),
  transitions = Map(
    (etat0, "[BP]") -> etat1,
    (etat1, "[FP]") -> etat2
  )
)

// Tests
println(reconnaitre(automate, List("[BP]")))        // Some(Set("Punch"))
println(reconnaitre(automate, List("[BP]", "[FP]"))) // Some(Set("Combo"))
println(reconnaitre(automate, List("[FP]")))         // None
```

---

## 🎓 Conclusion

Un automate fini, c'est :
- 📍 Un graphe d'états
- 🔀 Des transitions entre états
- 🎯 Des états finaux pour la reconnaissance
- ⚡ Une machine simple et efficace

Dans notre projet **ft_ality**, l'automate reconnaît les combos de jeu en suivant les transitions selon les touches tapées par l'utilisateur !

