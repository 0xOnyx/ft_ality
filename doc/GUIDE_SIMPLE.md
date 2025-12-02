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
    val (nomMouvement, symboles) = relge
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

## ⌨️ Étape 9 : Lire les touches du clavier EN TEMPS RÉEL (sans Entrée)

### Qu'est-ce qu'on fait ?

On lit les touches instantanément dès qu'elles sont pressées, **sans avoir besoin d'appuyer sur Entrée**. C'est essentiel pour un jeu de combat en temps réel !

### ⚠️ Important : Mode terminal raw

Par défaut, le terminal est en mode "cooked" (avec buffer). On doit le mettre en mode "raw" pour lire les touches immédiatement.

### Code pour lire les touches en temps réel

```scala
import java.io.{InputStream, IOException}
import scala.util.{Try, Success, Failure}

// Objet pour gérer le terminal en mode raw
object TerminalRaw {
  private var terminalConfig: Option[String] = None
  
  // Mettre le terminal en mode raw (désactiver le buffer)
  def activerModeRaw(): Either[String, Unit] = {
    val os = System.getProperty("os.name").toLowerCase
    try {
      if (os.contains("win")) {
        // Windows : utiliser la console native
        // Note: Sur Windows, c'est plus complexe, on utilise Runtime.exec
        val process = Runtime.getRuntime.exec("cmd.exe /c mode con: cols=80 lines=25")
        process.waitFor()
        Right(())
      } else {
        // Linux/Mac : utiliser stty pour mettre en mode raw
        val pb = new ProcessBuilder("sh", "-c", "stty -g < /dev/tty").redirectErrorStream(true)
        val process = pb.start()
        val config = scala.io.Source.fromInputStream(process.getInputStream).mkString.trim
        process.waitFor()
        
        terminalConfig = Some(config)
        
        // Mettre en mode raw
        val pbRaw = new ProcessBuilder("sh", "-c", "stty raw -echo < /dev/tty").redirectErrorStream(true)
        val processRaw = pbRaw.start()
        processRaw.waitFor()
        
        Right(())
      }
    } catch {
      case e: Exception => Left(s"Erreur activation mode raw: ${e.getMessage}")
    }
  }
  
  // Remettre le terminal en mode normal
  def desactiverModeRaw(): Unit = {
    val os = System.getProperty("os.name").toLowerCase
    try {
      if (os.contains("win")) {
        // Windows : réinitialiser
        Runtime.getRuntime.exec("cmd.exe /c mode con")
      } else {
        // Linux/Mac : restaurer la configuration
        terminalConfig match {
          case Some(config) =>
            val pb = new ProcessBuilder("sh", "-c", s"stty $config < /dev/tty").redirectErrorStream(true)
            pb.start().waitFor()
          case None =>
            // Mode par défaut
            val pb = new ProcessBuilder("sh", "-c", "stty cooked echo < /dev/tty").redirectErrorStream(true)
            pb.start().waitFor()
        }
      }
    } catch {
      case _: Exception => // Ignorer les erreurs de restauration
    }
  }
}

// Lire une touche en temps réel (sans Entrée)
def lireToucheTempsReel(): Option[Char] = {
  try {
    if (System.in.available() > 0) {
      val touche = System.in.read().toChar
      Some(touche)
    } else {
      None
    }
  } catch {
    case _: IOException => None
  }
}

// Lire une touche avec délai (non-bloquant)
def lireToucheAvecDelai(delaiMs: Long): Option[Char] = {
  val debut = System.currentTimeMillis()
  
  while (System.currentTimeMillis() - debut < delaiMs) {
    lireToucheTempsReel() match {
      case Some(touche) => return Some(touche)
      case None => Thread.sleep(10)  // Petit délai pour ne pas surcharger le CPU
    }
  }
  
  None  // Aucune touche dans le délai
}

// Convertir un caractère en String (pour compatibilité avec le mapping)
def charToString(c: Char): String = {
  c.toString.toLowerCase
}
```

### Version simplifiée (cross-platform)

Si la version avec `stty` est trop complexe, voici une version plus simple qui fonctionne sur tous les systèmes avec une bibliothèque Java standard :

```scala
import java.io.{InputStream, IOException}

// Lire une touche en temps réel (version simplifiée)
// Fonctionne mieux sur Linux/Mac, nécessite configuration sur Windows
object KeyboardReader {
  private var modeRawActive = false
  
  // Initialiser le mode raw
  def initialiser(): Either[String, Unit] = {
    val os = System.getProperty("os.name").toLowerCase
    
    if (os.contains("win")) {
      // Windows : utiliser jline ou bibliothèque native
      // Pour simplifier, on peut utiliser une bibliothèque comme jline3
      Left("Sur Windows, utilisez une bibliothèque comme jline3 pour le mode raw (voir section Alternative ci-dessous)")
    } else {
      // Linux/Mac : utiliser stty
      try {
        val process = Runtime.getRuntime.exec(Array("sh", "-c", "stty -g"))
        val config = scala.io.Source.fromInputStream(process.getInputStream).mkString.trim
        process.waitFor()
        
        // Sauvegarder la config puis activer raw
        Runtime.getRuntime.exec(Array("sh", "-c", s"stty -echo raw < /dev/tty")).waitFor()
        modeRawActive = true
        Right(())
      } catch {
        case e: Exception => Left(s"Impossible d'activer le mode raw: ${e.getMessage}")
      }
    }
  }
  
  // Restaurer le mode normal
  def restaurer(): Unit = {
    if (modeRawActive) {
      try {
        Runtime.getRuntime.exec(Array("sh", "-c", "stty echo cooked < /dev/tty")).waitFor()
        modeRawActive = false
      } catch {
        case _: Exception => // Ignorer
      }
    }
  }
  
  // Lire une touche (non-bloquant)
  def lireTouche(): Option[Char] = {
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

### Utilisation dans le code principal

```scala
// Au début du programme
KeyboardReader.initialiser() match {
  case Right(_) =>
    println("Mode temps réel activé !")
    // ... code du jeu ...
  case Left(erreur) =>
    println(s"Erreur: $erreur")
    sys.exit(1)
}

// À la fin du programme (important !)
try {
  // ... code du jeu ...
} finally {
  KeyboardReader.restaurer()  // Toujours restaurer le terminal !
}
```

### Alternative : Utiliser une bibliothèque (recommandé)

Pour une solution plus robuste et cross-platform, utilisez `jline3` :

```scala
// Ajouter dans build.sbt :
libraryDependencies += "org.jline" % "jline" % "3.21.0"

// Dans le code :
import org.jline.terminal.TerminalBuilder
import org.jline.terminal.Terminal

object KeyboardReader {
  private val terminal: Terminal = TerminalBuilder.builder()
    .system(true)
    .jna(true)
    .build()
  
  terminal.enterRawMode()
  
  def lireTouche(): Option[Char] = {
    val reader = terminal.reader()
    if (reader.ready()) {
      Some(reader.read().toChar)
    } else {
      None
    }
  }
  
  def fermer(): Unit = {
    terminal.close()
  }
}
```

**Avantage** : `jline3` gère automatiquement Windows, Linux et Mac !

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

## 🎮 Étape 11 : La boucle principale (FONCTIONNEL) avec délai pour les combos

### Qu'est-ce qu'on fait ?

On fait une boucle infinie qui :
1. Lit une touche
2. Convertit la touche en symbole
3. Utilise le symbole dans l'automate
4. **Si on atteint un état final, on attend un délai avant d'afficher** (pour permettre les combos longs)
5. Affiche le mouvement si reconnu

### ⏱️ Pourquoi un délai ?

Imagine que tu as deux combos :
- "Punch" = `[BP]` (1 touche)
- "Combo" = `[BP], [FP]` (2 touches)

Si tu tapes rapidement "d" puis "x" :
- Sans délai : Dès que `[BP]` arrive → Affiche "Punch" → Réinitialise → Le combo `[BP], [FP]` n'est jamais reconnu ❌
- Avec délai : Dès que `[BP]` arrive → Attend 300ms → Si `[FP]` arrive avant, continue → Reconnu "Combo" ✅

### Code fonctionnel pur avec délai (récursion terminale)

```scala
import scala.annotation.tailrec

// Constante : délai en millisecondes avant de conclure qu'un combo est fini
val DELAI_COMBO_MS = 300  // 300ms = 0.3 secondes

// La boucle principale (FONCTIONNEL) avec délai
def bouclePrincipale(automate: Automate, mapping: Map[String, String]): Unit = {
  // Fonction récursive interne (récursion terminale)
  @tailrec
  def boucleLoop(
    etatActuel: Etat,
    buffer: List[String],  // Buffer pour afficher la séquence
    dernierTemps: Long = System.currentTimeMillis()  // Temps de la dernière touche
  ): Unit = {
    val maintenant = System.currentTimeMillis()
    val tempsDepuisDerniereTouche = maintenant - dernierTemps
    
    // Si on est dans un état final ET qu'il s'est passé assez de temps
    // → L'utilisateur a fini son combo, on peut afficher
    if (automate.etatsFinaux.contains(etatActuel) && tempsDepuisDerniereTouche > DELAI_COMBO_MS) {
      // Afficher la séquence
      println(buffer.mkString(", "))
      println()
      
      // Afficher les mouvements reconnus
      etatActuel.mouvements.foreach { mouvement =>
        println(s"$mouvement !!")
      }
      println()
      
      // Réinitialiser (récursion avec état initial)
      boucleLoop(automate.etatInitial, List.empty, maintenant)
    } else {
      // 1. Lire une touche en temps réel (non-bloquant)
      val touche = KeyboardReader.lireTouche()  // Lit instantanément sans Entrée
      
      touche match {
        case Some(keyChar) =>
          // 2. Convertir caractère -> String puis -> symbole
          val key = keyChar.toString.toLowerCase
          mapping.get(key) match {
            case Some(sym) =>
              val nouveauBuffer = buffer :+ sym
              
              // 3. Chercher la transition
              automate.transitions.get((etatActuel, sym)) match {
                case Some(nouvelEtat) =>
                  // Transition trouvée : continuer avec le nouvel état
                  boucleLoop(nouvelEtat, nouveauBuffer, maintenant)
                case None =>
                  // Pas de transition : rester dans le même état
                  boucleLoop(etatActuel, nouveauBuffer, maintenant)
              }
            case None =>
              // Touche non mappée : ignorer
              boucleLoop(etatActuel, buffer, dernierTemps)
          }
        case None =>
          // Pas de touche : continuer à attendre (petit délai pour ne pas surcharger le CPU)
          Thread.sleep(10)
          boucleLoop(etatActuel, buffer, dernierTemps)
      }
    }
  }
  
  // Commencer la récursion depuis l'état initial
  boucleLoop(automate.etatInitial, List.empty)
}
```

**Explication étape par étape (FONCTIONNEL)** :

1. **Fonction récursive avec temps** :
   ```scala
   @tailrec
   def boucleLoop(etatActuel: Etat, buffer: List[String], dernierTemps: Long)
   ```
   - On ajoute `dernierTemps` pour savoir quand la dernière touche a été tapée
   - Pas de `var` : tout est passé en paramètres

2. **Vérifier le délai si état final** :
   ```scala
   if (automate.etatsFinaux.contains(etatActuel) && tempsDepuisDerniereTouche > DELAI_COMBO_MS)
   ```
   - Si on est dans un état final ET qu'il s'est passé 300ms depuis la dernière touche
   - → Le combo est fini, on peut afficher et réinitialiser

3. **Lire une touche en temps réel** :
   ```scala
   val touche = KeyboardReader.lireTouche()
   ```
   - Lit instantanément les touches **sans avoir besoin d'Entrée**
   - Non-bloquant : retourne `None` si aucune touche
   - **Important** : Il faut avoir appelé `KeyboardReader.initialiser()` au début du programme !

4. **Si une touche arrive** :
   - On convertit en symbole
   - On suit la transition
   - On met à jour `dernierTemps` à `maintenant` (la touche vient d'arriver)

5. **Résultat** :
   - Si tu tapes rapidement plusieurs touches : elles sont toutes traitées avant le délai
   - Si tu attends 300ms sans touche : le combo est affiché

### Exemple concret avec délai

**Automate** :
```
[0] --[BP]--> [1] ✅ (Punch)
              |
              | [FP]
              ▼
              [2] ✅ (Combo)
```

**Scénario : Tu tapes rapidement "d" (→ [BP]) puis "x" (→ [FP])**

#### Timeline avec délai :

```
Temps   | Action                        | État  | Décision
--------|-------------------------------|-------|-------------------------
0ms     | Tape "d" → [BP]               | 0→1   | État 1 est FINAL
        |                               |       | ⏱️ Attend 300ms...
50ms    | Tape "x" → [FP] (rapide!)    | 1→2   | Nouvelle touche arrive !
        |                               |       | Continuer (temps < 300ms)
350ms   | Pas de nouvelle touche        | 2     | 300ms écoulés depuis x
        |                               |       | ✅ Affiche "Combo !!"
        |                               |       | Réinitialise à État 0
```

**Résultat** : Le combo `[BP], [FP]` est reconnu ! ✅

#### Comparaison : Avec vs Sans délai

**SANS délai (ancienne version)** :
```
Temps   | Action              | État  | Décision
--------|---------------------|-------|------------------
0ms     | Tape "d" → [BP]     | 0→1   | État 1 FINAL
        |                     |       | ❌ Affiche "Punch" immédiatement
        |                     |       | Réinitialise
50ms    | Tape "x" → [FP]     | 0     | ❌ Trop tard, déjà réinitialisé
        |                     |       | Pas de transition depuis État 0
```

**Résultat** : Seul "Punch" est reconnu, le combo est perdu ! ❌

**AVEC délai (nouvelle version)** :
```
Temps   | Action              | État  | Décision
--------|---------------------|-------|------------------
0ms     | Tape "d" → [BP]     | 0→1   | État 1 FINAL
        |                     |       | ⏱️ Attend 300ms...
50ms    | Tape "x" → [FP]     | 1→2   | ✅ Nouvelle touche arrive
        |                     |       | Continue le combo
350ms   | Pas de touche       | 2     | ⏱️ 300ms écoulés
        |                     |       | ✅ Affiche "Combo !!"
```

**Résultat** : Le combo complet est reconnu ! ✅

### 💡 Ajuster le délai

Le délai de 300ms est une valeur par défaut qui fonctionne bien pour la plupart des jeux. Tu peux l'ajuster :

```scala
val DELAI_COMBO_MS = 300  // Délai par défaut : 300ms

// Pour des combos plus rapides (jeux de rythme)
val DELAI_RAPIDE = 200  // 200ms

// Pour des combos plus lents (jeux plus tactiques)
val DELAI_LENT = 500  // 500ms
```

**Conseils** :
- **Délai trop court (< 200ms)** : Difficile de taper rapidement, risque de reconnaître trop tôt
- **Délai trop long (> 500ms)** : Trop d'attente, le jeu semble lent
- **300ms** : Un bon compromis pour la plupart des jeux de combat

### Version simplifiée avec readLine() (pour débuter)

Si la version avec délai semble complexe, voici une version simplifiée qui utilise `readLine()` :

```scala
import scala.annotation.tailrec

// Version simplifiée : lit une touche, puis vérifie le délai
def bouclePrincipaleSimple(automate: Automate, mapping: Map[String, String]): Unit = {
  @tailrec
  def boucleLoop(etatActuel: Etat, buffer: List[String]): Unit = {
    // Lire une touche (bloque jusqu'à Entrée)
    val touche = lireToucheSimple()
    
    touche match {
      case Some(key) =>
        mapping.get(key) match {
          case Some(sym) =>
            val nouveauBuffer = buffer :+ sym
            
            automate.transitions.get((etatActuel, sym)) match {
              case Some(nouvelEtat) =>
                if (automate.etatsFinaux.contains(nouvelEtat)) {
                  // État final atteint
                  println(nouveauBuffer.mkString(", "))
                  println()
                  nouvelEtat.mouvements.foreach { mouvement =>
                    println(s"$mouvement !!")
                  }
                  println()
                  
                  // Pour les combos, on attend un peu avant de réinitialiser
                  // Ici, avec readLine(), on attend simplement la prochaine touche
                  boucleLoop(automate.etatInitial, List.empty)
                } else {
                  boucleLoop(nouvelEtat, nouveauBuffer)
                }
              case None =>
                boucleLoop(etatActuel, nouveauBuffer)
            }
          case None =>
            boucleLoop(etatActuel, buffer)
        }
      case None =>
        boucleLoop(etatActuel, buffer)
    }
  }
  
  boucleLoop(automate.etatInitial, List.empty)
}
```

**Note** : Avec `readLine()`, le délai est géré naturellement car chaque touche nécessite un appui sur Entrée. Pour les combos, l'utilisateur peut taper plusieurs touches avant d'appuyer sur Entrée, ou taper touche par touche rapidement.

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
          
          // 6. Initialiser le mode temps réel (lecture sans Entrée)
          KeyboardReader.initialiser() match {
            case Right(_) =>
              try {
                // 7. Lancer la boucle
                bouclePrincipale(automate, mapping)
              } finally {
                // IMPORTANT : Toujours restaurer le terminal avant de quitter !
                KeyboardReader.restaurer()
              }
            case Left(erreur) =>
              println(s"Erreur activation mode temps réel: $erreur")
              println("Fonctionnement limité (lecture avec Entrée)")
              sys.exit(1)
          }
          
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

