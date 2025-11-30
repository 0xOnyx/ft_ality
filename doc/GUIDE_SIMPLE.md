# Guide ultra-simple : Comment ça marche ? (PROGRAMMATION FONCTIONNELLE)

## ⚠️ IMPORTANT : Tout est en programmation fonctionnelle pure

- ❌ **PAS de `var`** : On utilise uniquement `val`
- ❌ **PAS de `while`** : On utilise la récursion terminale (`@tailrec`)
- ❌ **PAS de mutation** : On crée de nouvelles structures au lieu de modifier
- ✅ **Fonctions pures** : Pas d'effets de bord (sauf I/O nécessaire)
- ✅ **Récursion terminale** : Toutes les boucles sont en récursion optimisée

## 🎯 Le but du projet

Tu veux créer un programme qui :
1. Lit un fichier avec des combos de jeu (ex: "Punch: [BP]")
2. Construit un automate qui reconnaît ces combos
3. Attend que l'utilisateur tape des touches
4. Affiche le nom du combo quand il est reconnu

---

## 📝 Étape 1 : Lire le fichier de grammaire (FONCTIONNEL)

### Qu'est-ce qu'on fait ?

On lit un fichier texte qui contient des règles comme ça :
```
Claw Slam: [BP]
Combo: [BP], [FP]
```

### Code fonctionnel (gestion d'erreurs avec Either)

```scala
// Fonction pour lire un fichier ligne par ligne (FONCTIONNEL)
def lireFichier(chemin: String): Either[String, List[String]] = {
  try {
    val source = scala.io.Source.fromFile(chemin)
    val lignes = source.getLines().toList
    source.close()
    Right(lignes)  // ✅ Succès : retourne les lignes
  } catch {
    case e: Exception =>
      Left(s"Erreur : ${e.getMessage}")  // ❌ Erreur : retourne un message
  }
}
```

**Explication** :
- `Either[String, List[String]]` : Retourne soit une erreur (`Left`), soit le résultat (`Right`)
- `Right(lignes)` : Si ça marche, on retourne les lignes dans un `Right`
- `Left(message)` : Si ça échoue, on retourne un message d'erreur dans un `Left`
- Pas d'exception : On utilise `Either` pour gérer les erreurs de manière fonctionnelle

**Utilisation** :
```scala
lireFichier("grammars/mk9.gmr") match {
  case Right(lignes) =>
    // ✅ Fichier lu avec succès
    println(s"${lignes.length} lignes lues")
  case Left(erreur) =>
    // ❌ Erreur
    println(erreur)
}
```

---

## 🔪 Étape 2 : Découper chaque ligne

### Qu'est-ce qu'on fait ?

On prend une ligne comme `"Claw Slam: [BP]"` et on la découpe en deux parties :
- Le nom : `"Claw Slam"`
- La séquence : `"[BP]"`

### Code simple

```scala
// Découper une ligne en nom et séquence
def decouperLigne(ligne: String): (String, String) = {
  val parties = ligne.split(":")  // Découpe au niveau du ":"
  val nom = parties(0).trim        // Partie avant ":" = nom
  val sequence = parties(1).trim   // Partie après ":" = séquence
  (nom, sequence)
}
```

**Explication** :
- `split(":")` : Découpe la chaîne au niveau de `":"`
  - `"Claw Slam: [BP]".split(":")` → `Array("Claw Slam", " [BP]")`
- `trim` : Enlève les espaces au début et à la fin
- Retourne : `("Claw Slam", "[BP]")`

**Exemple** :
```scala
val ligne = "Claw Slam: [BP]"
val (nom, sequence) = decouperLigne(ligne)
// nom = "Claw Slam"
// sequence = "[BP]"
```

---

## 🧩 Étape 3 : Découper la séquence en symboles

### Qu'est-ce qu'on fait ?

On prend une séquence comme `"[BP], [FP]"` et on la découpe en symboles :
- `"[BP]"`
- `"[FP]"`

### Code simple

```scala
// Découper une séquence en symboles
def decouperSequence(sequence: String): List[String] = {
  sequence
    .split(",")           // Découpe au niveau des virgules
    .map(_.trim)          // Enlève les espaces de chaque partie
    .toList               // Convertit en liste
}
```

**Explication** :
- `split(",")` : Découpe au niveau des virgules
  - `"[BP], [FP]".split(",")` → `Array("[BP]", " [FP]")`
- `map(_.trim)` : Pour chaque partie, enlève les espaces
  - `Array("[BP]", " [FP]")` → `Array("[BP]", "[FP]")`
- `toList` : Convertit en liste Scala
- Retourne : `List("[BP]", "[FP]")`

### 🔍 Explication détaillée : C'est quoi le `_` dans `map(_.trim)` ?

Le `_` est un raccourci en Scala pour écrire une fonction anonyme (lambda).

**Version longue (sans `_`)** :
```scala
.map(element => element.trim)
```

**Version courte (avec `_`)** :
```scala
.map(_.trim)
```

**C'est exactement la même chose !**

**Explication étape par étape** :

1. **`map`** : C'est une fonction qui prend chaque élément d'une liste et lui applique une fonction
   ```scala
   List("  [BP]  ", "  [FP]  ").map(...)
   ```

2. **`_.trim`** : C'est une fonction anonyme qui dit "prends l'élément (représenté par `_`) et appelle `.trim` dessus"
   - `_` = l'élément actuel de la liste
   - `.trim` = enlève les espaces au début et à la fin

3. **Résultat** :
   ```scala
   List("  [BP]  ", "  [FP]  ").map(_.trim)
   // Devient :
   List("[BP]", "[FP]")
   ```

**Exemples pour comprendre** :

```scala
// Exemple 1 : Avec une liste de nombres
List(1, 2, 3).map(_ * 2)
// _ = chaque nombre
// Résultat : List(2, 4, 6)

// Exemple 2 : Avec des chaînes
List("hello", "world").map(_.toUpperCase)
// _ = chaque chaîne
// Résultat : List("HELLO", "WORLD")

// Exemple 3 : Notre cas
Array("  [BP]  ", "  [FP]  ").map(_.trim)
// _ = chaque élément du tableau
// Résultat : Array("[BP]", "[FP]")
```

**Si tu veux être plus explicite** (version longue) :
```scala
// Version avec fonction explicite
def decouperSequence(sequence: String): List[String] = {
  sequence
    .split(",")
    .map(element => element.trim)  // Version longue
    .toList
}
```

**Les deux versions font exactement la même chose !**

**Exemple** :
```scala
val sequence = "[BP], [FP]"
val symboles = decouperSequence(sequence)
// symboles = List("[BP]", "[FP]")
```

### 🔍 Explication détaillée : C'est quoi `match` ?

Le `match` en Scala, c'est comme un **"si... alors... sinon"** mais en mieux !

**Exemple simple** :
```scala
val nombre = 5

nombre match {
  case 1 => println("Un")
  case 2 => println("Deux")
  case 3 => println("Trois")
  case _ => println("Autre")  // _ = "tout le reste"
}
```

**Dans notre code, on utilise `match` avec `Option`** :
```scala
val resultat: Option[String] = Some("Bonjour")

resultat match {
  case Some(valeur) =>
    println(s"J'ai trouvé : $valeur")
  case None =>
    println("Rien trouvé")
}
```

**Explication** :
- `case Some(valeur)` : Si c'est `Some`, on récupère la valeur
- `case None` : Si c'est `None`, on fait autre chose

**Exemple dans notre projet** :
```scala
automate.transitions.get((etatActuel, symbole)) match {
  case Some(nouvelEtat) =>
    // Transition trouvée : on continue avec nouvelEtat
    reconnaitreLoop(nouvelEtat, suite)
  case None =>
    // Pas de transition : pas reconnu
    None
}
```

👉 **Voir `EXPLICATION_MATCH.md` pour plus de détails !**

---

## 🏗️ Étape 4 : Créer un état

### Qu'est-ce qu'un état ?

Un état, c'est comme une "position" dans l'automate. Chaque état a :
- Un numéro (0, 1, 2, ...)
- Est-ce que c'est un état final ? (oui/non)
- Quels mouvements sont reconnus à cet état ?

### Code simple

```scala
// Définition d'un état
case class Etat(
  id: Int,                    // Numéro de l'état (0, 1, 2, ...)
  estFinal: Boolean,          // true si c'est un état final
  mouvements: Set[String]    // Liste des mouvements reconnus
)
```

**Explication** :
- `case class` : C'est une structure de données immuable (on ne peut pas la modifier)
- `id` : Le numéro de l'état
- `estFinal` : `true` si on peut reconnaître un mouvement à cet état
- `mouvements` : Les noms des mouvements reconnus (ex: `Set("Claw Slam")`)

**Exemple** :
```scala
val etat0 = Etat(0, false, Set())           // État initial, pas final
val etat1 = Etat(1, true, Set("Claw Slam")) // État final, reconnaît "Claw Slam"
```

---

## 🔀 Étape 5 : Créer une transition

### Qu'est-ce qu'une transition ?

Une transition, c'est une "flèche" qui dit :
"Si je suis à l'état X et que je lis le symbole Y, je vais à l'état Z"

### Code simple

```scala
// Une transition, c'est juste une Map
// Clé : (état_départ, symbole)
// Valeur : état_arrivée

val transitions = Map(
  (etat0, "[BP]") -> etat1  // De l'état 0, avec "[BP]", on va à l'état 1
)
```

**Explication** :
- `Map` : C'est comme un dictionnaire en Python
- Clé : `(etat0, "[BP]")` = un couple (état de départ, symbole)
- Valeur : `etat1` = l'état d'arrivée

**Exemple visuel** :
```
État 0 --[BP]--> État 1
```

---

## 🤖 Étape 6 : Créer l'automate

### Qu'est-ce qu'un automate ?

Un automate, c'est :
- Un état de départ
- Des états finaux (où on reconnaît les mouvements)
- Des transitions (les flèches)

### Code simple

```scala
// Définition de l'automate
case class Automate(
  etatInitial: Etat,                              // État de départ
  etatsFinaux: Set[Etat],                         // États où on reconnaît
  transitions: Map[(Etat, String), Etat]          // Les flèches
)
```

**Explication** :
- `etatInitial` : L'état où on commence (toujours l'état 0)
- `etatsFinaux` : Tous les états où on peut reconnaître un mouvement
- `transitions` : Toutes les flèches de l'automate

**Exemple** :
```scala
val automate = Automate(
  etatInitial = Etat(0, false, Set()),
  etatsFinaux = Set(Etat(1, true, Set("Claw Slam"))),
  transitions = Map(
    (Etat(0, false, Set()), "[BP]") -> Etat(1, true, Set("Claw Slam"))
  )
)
```

---

## 🏭 Étape 7 : Construire l'automate à partir des règles (FONCTIONNEL)

### Qu'est-ce qu'on fait ?

On prend les règles qu'on a lues et on construit l'automate petit à petit.

### Code fonctionnel pur (sans var, sans while)

```scala
import scala.annotation.tailrec

// Structure pour garder l'état de construction (immuable)
case class Construction(
  etatCourant: Int,                                    // Prochain numéro d'état
  etats: Set[Etat],                                    // Tous les états créés
  transitions: Map[(Etat, String), Etat],             // Toutes les transitions
  etatsFinaux: Set[Etat]                              // Tous les états finaux
)

// Construire l'automate à partir d'une liste de règles (FONCTIONNEL)
def construireAutomate(regles: List[(String, List[String])]): Automate = {
  // 1. Créer l'état initial
  val etat0 = Etat(0, false, Set())
  val constructionInitiale = Construction(
    etatCourant = 0,
    etats = Set(etat0),
    transitions = Map.empty,
    etatsFinaux = Set.empty
  )
  
  // 2. Ajouter chaque règle une par une (avec foldLeft)
  val constructionFinale = regles.foldLeft(constructionInitiale) { (construction, regle) =>
    val (nomMouvement, symboles) = regle
    ajouterRegle(construction, nomMouvement, symboles, etat0)
  }
  
  // 3. Retourner l'automate
  Automate(etat0, constructionFinale.etatsFinaux, constructionFinale.transitions)
}

// Ajouter une règle à la construction (FONCTIONNEL)
def ajouterRegle(
  construction: Construction,
  nomMouvement: String,
  symboles: List[String],
  etatInitial: Etat
): Construction = {
  // Fonction récursive pour parcourir les symboles
  @tailrec
  def ajouterSequence(
    const: Construction,
    etatActuel: Etat,
    reste: List[String]
  ): (Construction, Etat) = {
    reste match {
      case Nil =>
        // Fin de la séquence : marquer l'état comme final
        val etatFinal = etatActuel.copy(
          estFinal = true,
          mouvements = etatActuel.mouvements + nomMouvement
        )
        val nouvelleConst = const.copy(
          etats = const.etats - etatActuel + etatFinal,
          etatsFinaux = const.etatsFinaux + etatFinal
        )
        (nouvelleConst, etatFinal)
        
      case symbole :: suite =>
        // Vérifier si une transition existe déjà
        const.transitions.get((etatActuel, symbole)) match {
          case Some(etatExistant) =>
            // Transition existe : réutiliser l'état
            ajouterSequence(const, etatExistant, suite)
          case None =>
            // Pas de transition : créer un nouvel état
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
  
  val (nouvelleConstruction, _) = ajouterSequence(construction, etatInitial, symboles)
  nouvelleConstruction
}
```

**Explication étape par étape (FONCTIONNEL)** :

1. **Créer l'état initial** :
   ```scala
   val etat0 = Etat(0, false, Set())
   ```
   - On crée l'état 0 (le point de départ)

2. **Pour chaque règle (avec foldLeft)** :
   ```scala
   regles.foldLeft(constructionInitiale) { (construction, regle) => ... }
   ```
   - `foldLeft` : On part de la construction initiale
   - Pour chaque règle, on appelle `ajouterRegle`
   - On obtient une nouvelle construction (pas de mutation !)

3. **Parcourir les symboles (récursion terminale)** :
   ```scala
   @tailrec
   def ajouterSequence(const: Construction, etatActuel: Etat, reste: List[String])
   ```
   - On utilise la récursion au lieu d'une boucle `for`
   - `@tailrec` : Scala vérifie que c'est bien une récursion terminale (optimisée)

4. **Vérifier si la transition existe** :
   ```scala
   const.transitions.get((etatActuel, symbole))
   ```
   - On cherche s'il y a déjà une flèche depuis l'état actuel avec ce symbole
   - Si oui : on réutilise l'état (récursion avec l'état existant)
   - Si non : on crée un nouvel état (récursion avec le nouvel état)

5. **Marquer comme final** :
   ```scala
   val etatFinal = etatActuel.copy(estFinal = true, ...)
   ```
   - Le dernier état devient un état final
   - On y ajoute le nom du mouvement
   - On retourne une NOUVELLE construction (pas de modification !)

**Exemple concret** :

Règle : `("Claw Slam", List("[BP]"))`

1. On est à l'état 0
2. On lit `"[BP]"`
3. Pas de transition `(0, "[BP]")` → On crée l'état 1
4. On crée la transition `(0, "[BP]") -> 1`
5. L'état 1 devient final avec `mouvements = Set("Claw Slam")`

Résultat :
```
État 0 --[BP]--> État 1 (FINAL: "Claw Slam")
```

---

## 🔍 Étape 8 : Reconnaître une séquence (FONCTIONNEL)

### Qu'est-ce qu'on fait ?

On prend une séquence de symboles (ex: `List("[BP]")`) et on vérifie si l'automate la reconnaît.

### Code fonctionnel pur (récursion terminale)

```scala
import scala.annotation.tailrec

// Reconnaître une séquence (FONCTIONNEL)
def reconnaitre(automate: Automate, sequence: List[String]): Option[Set[String]] = {
  // Fonction récursive interne
  @tailrec
  def reconnaitreLoop(
    etatActuel: Etat,
    reste: List[String]
  ): Option[Set[String]] = {
    reste match {
      case Nil =>
        // Fin de la séquence : vérifier si on est dans un état final
        if (automate.etatsFinaux.contains(etatActuel)) {
          Some(etatActuel.mouvements)  // ✅ Reconnu !
        } else {
          None  // ❌ Non reconnu
        }
        
      case symbole :: suite =>
        // Chercher la transition
        automate.transitions.get((etatActuel, symbole)) match {
          case Some(nouvelEtat) =>
            // Transition trouvée : continuer avec le nouvel état
            reconnaitreLoop(nouvelEtat, suite)
          case None =>
            // Pas de transition : la séquence n'est pas reconnue
            None
        }
    }
  }
  
  // Commencer la récursion depuis l'état initial
  reconnaitreLoop(automate.etatInitial, sequence)
}
```

**Explication étape par étape (FONCTIONNEL)** :

1. **Fonction récursive** :
   ```scala
   @tailrec
   def reconnaitreLoop(etatActuel: Etat, reste: List[String])
   ```
   - On utilise la récursion au lieu d'une boucle `for`
   - `@tailrec` : Scala vérifie que c'est optimisé
   - Pas de `var` : tout est passé en paramètres

2. **Pattern matching sur la liste** :
   ```scala
   reste match {
     case Nil => ...  // Liste vide : fin
     case symbole :: suite => ...  // Premier élément + reste
   }
   ```
   - Si la liste est vide (`Nil`) : on a fini de lire
   - Sinon : on prend le premier symbole et le reste

3. **Chercher la transition** :
   ```scala
   automate.transitions.get((etatActuel, symbole))
   ```
   - On cherche s'il y a une flèche depuis l'état actuel avec ce symbole
   - Si oui : on continue la récursion avec le nouvel état
   - Si non : on retourne `None` (pas reconnu)

4. **Vérifier si on est dans un état final** :
   ```scala
   if (automate.etatsFinaux.contains(etatActuel))
   ```
   - Quand la liste est vide, on vérifie si on est dans un état final
   - Si oui : on retourne les mouvements reconnus
   - Si non : on retourne `None`

**Exemple concret** :

Séquence : `List("[BP]")`
Automate : État 0 --[BP]--> État 1 (FINAL: "Claw Slam")

1. `etatActuel = État 0`
2. Symbole `"[BP]"` → Transition `(0, "[BP]")` existe → `etatActuel = État 1`
3. Fin de la séquence
4. État 1 est final → Retourne `Some(Set("Claw Slam"))` ✅

---

## ⌨️ Étape 9 : Lire les touches du clavier

### Qu'est-ce qu'on fait ?

On attend que l'utilisateur tape une touche et on la lit.

### Code simple

```scala
// Lire une touche du clavier
def lireTouche(): Option[String] = {
  try {
    val ligne = scala.io.StdIn.readLine()
    if (ligne == null || ligne.isEmpty) {
      None  // Pas de touche
    } else {
      Some(ligne.trim)  // La touche tapée
    }
  } catch {
    case _: Exception => None
  }
}
```

**Explication** :
- `scala.io.StdIn.readLine()` : Attend que l'utilisateur tape quelque chose et appuie sur Entrée
- `trim` : Enlève les espaces
- Retourne `Some("d")` si l'utilisateur a tapé "d", ou `None` s'il n'y a rien

**Note** : En vrai, pour un jeu de combat, on veut lire les touches en temps réel (sans appuyer sur Entrée). Mais pour commencer, cette version simple fonctionne.

---

## 🗺️ Étape 10 : Créer le key mapping

### Qu'est-ce qu'un key mapping ?

C'est un dictionnaire qui dit :
- Si l'utilisateur tape `"d"` → ça correspond au symbole `"[BP]"`
- Si l'utilisateur tape `"x"` → ça correspond au symbole `"[FP]"`

### Code simple

```scala
// Créer le key mapping automatiquement
def creerKeyMapping(automate: Automate): Map[String, String] = {
  // 1. Extraire tous les symboles de l'automate
  val symboles = automate.transitions.keys.map(_._2).toSet
  // Résultat : Set("[BP]", "[FP]")
  
  // 2. Liste des touches disponibles
  val touches = List("q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
                     "a", "s", "d", "f", "g", "h", "j", "k", "l",
                     "z", "x", "c", "v", "b", "n", "m")
  
  // 3. Associer chaque symbole à une touche
  val mapping = symboles.zip(touches).toMap.map(_.swap)
  // Résultat : Map("d" -> "[BP]", "x" -> "[FP]")
  
  mapping
}
```

**Explication étape par étape** :

1. **Extraire les symboles** :
   ```scala
   automate.transitions.keys.map(_._2)
   ```
   - On prend toutes les transitions
   - On extrait le symbole (le deuxième élément du couple)
   - On enlève les doublons avec `.toSet`

2. **Associer aux touches** :
   ```scala
   symboles.zip(touches)
   ```
   - On associe le premier symbole à la première touche
   - Le deuxième symbole à la deuxième touche
   - etc.

3. **Inverser** :
   ```scala
   .map(_.swap)
   ```
   - On veut `Map(touche -> symbole)` et pas `Map(symbole -> touche)`

**Exemple** :
```scala
symboles = Set("[BP]", "[FP]")
touches = List("d", "x", ...)
mapping = Map("d" -> "[BP]", "x" -> "[FP]")
```

---

## 🎮 Étape 11 : La boucle principale (FONCTIONNEL)

### Qu'est-ce qu'on fait ?

On fait une boucle infinie qui :
1. Lit une touche
2. Convertit la touche en symbole
3. Utilise le symbole dans l'automate
4. Affiche le mouvement si reconnu

### Code fonctionnel pur (récursion terminale)

```scala
import scala.annotation.tailrec

// La boucle principale (FONCTIONNEL)
def bouclePrincipale(automate: Automate, mapping: Map[String, String]): Unit = {
  // Fonction récursive interne (récursion terminale)
  @tailrec
  def boucleLoop(
    etatActuel: Etat,
    buffer: List[String]  // Buffer pour afficher la séquence
  ): Unit = {
    // 1. Lire une touche
    val touche = lireTouche()
    
    touche match {
      case Some(key) =>
        // 2. Convertir touche -> symbole
        mapping.get(key) match {
          case Some(sym) =>
            val nouveauBuffer = buffer :+ sym
            
            // 3. Chercher la transition
            automate.transitions.get((etatActuel, sym)) match {
              case Some(nouvelEtat) =>
                // 4. Vérifier si on est dans un état final
                if (automate.etatsFinaux.contains(nouvelEtat)) {
                  // 5. Afficher la séquence
                  println(nouveauBuffer.mkString(", "))
                  println()
                  
                  // 6. Afficher les mouvements
                  nouvelEtat.mouvements.foreach { mouvement =>
                    println(s"$mouvement !!")
                  }
                  println()
                  
                  // 7. Réinitialiser (récursion avec état initial)
                  boucleLoop(automate.etatInitial, List.empty)
                } else {
                  // Continuer avec le nouvel état
                  boucleLoop(nouvelEtat, nouveauBuffer)
                }
              case None =>
                // Pas de transition : rester dans le même état
                boucleLoop(etatActuel, nouveauBuffer)
            }
          case None =>
            // Touche non mappée : ignorer
            boucleLoop(etatActuel, buffer)
        }
      case None =>
        // Pas de touche : attendre (récursion avec même état)
        boucleLoop(etatActuel, buffer)
    }
  }
  
  // Commencer la récursion depuis l'état initial
  boucleLoop(automate.etatInitial, List.empty)
}
```

**Explication étape par étape (FONCTIONNEL)** :

1. **Fonction récursive** :
   ```scala
   @tailrec
   def boucleLoop(etatActuel: Etat, buffer: List[String])
   ```
   - On utilise la récursion au lieu de `while(true)`
   - `@tailrec` : Scala vérifie que c'est optimisé
   - Pas de `var` : tout est passé en paramètres

2. **Lire une touche** :
   ```scala
   val touche = lireTouche()
   ```
   - On attend que l'utilisateur tape quelque chose

3. **Convertir en symbole** :
   ```scala
   mapping.get(key)
   ```
   - Si l'utilisateur tape `"d"`, on cherche dans le mapping
   - On trouve `"[BP]"`

4. **Suivre la transition** :
   ```scala
   automate.transitions.get((etatActuel, sym))
   ```
   - On cherche s'il y a une flèche depuis l'état actuel avec ce symbole
   - Si oui, on continue la récursion avec le nouvel état

5. **Vérifier si c'est final** :
   ```scala
   if (automate.etatsFinaux.contains(nouvelEtat))
   ```
   - Si on est dans un état final, on a reconnu un mouvement !

6. **Afficher** :
   ```scala
   nouvelEtat.mouvements.foreach { mouvement =>
     println(s"$mouvement !!")
   }
   ```
   - On affiche tous les mouvements reconnus

7. **Réinitialiser (récursion)** :
   ```scala
   boucleLoop(automate.etatInitial, List.empty)
   ```
   - On relance la récursion avec l'état initial (pas de mutation !)

---

## 🎯 Étape 12 : Tout assembler (FONCTIONNEL)

### Code complet fonctionnel

```scala
@main
def main(args: Array[String]): Unit = {
  // Vérifier les arguments
  args.headOption match {
    case Some(cheminFichier) =>
      // 1. Lire le fichier (avec gestion d'erreurs)
      lireFichier(cheminFichier) match {
        case Right(lignes) =>
          // 2. Parser chaque ligne
          val regles = lignes.map { ligne =>
            val (nom, sequence) = decouperLigne(ligne)
            val symboles = decouperSequence(sequence)
            (nom, symboles)
          }
          
          // 3. Construire l'automate
          val automate = construireAutomate(regles)
          
          // 4. Créer le key mapping
          val mapping = creerKeyMapping(automate)
          
          // 5. Afficher les mappings
          println("Key mappings:")
          println()
          mapping.toList.sortBy(_._1).foreach { case (key, symbol) =>
            println(s"$key -> $symbol")
          }
          println("----------------------")
          println()
          
          // 6. Lancer la boucle
          bouclePrincipale(automate, mapping)
          
        case Left(erreur) =>
          // Erreur de lecture du fichier
          println(erreur)
          sys.exit(1)
      }
    case None =>
      // Pas d'argument
      println("Usage: ./ft_ality <grammar_file.gmr>")
      sys.exit(1)
  }
}
```

**Explication étape par étape** :

1. **Lire le fichier** :
   - On lit le fichier de grammaire ligne par ligne

2. **Parser** :
   - Pour chaque ligne, on extrait le nom et la séquence
   - On découpe la séquence en symboles

3. **Construire l'automate** :
   - On construit l'automate à partir des règles

4. **Créer le mapping** :
   - On crée automatiquement le mapping touche -> symbole

5. **Afficher** :
   - On affiche les mappings pour que l'utilisateur sache quelles touches utiliser

6. **Boucle** :
   - On lance la boucle infinie qui attend les touches

---

## 📋 Résumé ultra-simple

1. **Lire le fichier** → Liste de règles
2. **Parser les règles** → Nom + Liste de symboles
3. **Construire l'automate** → États + Transitions
4. **Créer le mapping** → Touche → Symbole
5. **Afficher les mappings**
6. **Boucle infinie** :
   - Lire touche
   - Convertir en symbole
   - Suivre transition dans l'automate
   - Si état final → Afficher mouvement

C'est tout ! 🎉

