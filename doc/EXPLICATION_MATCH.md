# Explication simple : C'est quoi `match` en Scala ?

## 🎯 Le `match` en Scala

Le `match` en Scala, c'est comme un **"si... alors... sinon"** mais en beaucoup plus puissant !

C'est l'équivalent du `switch` en Java/C, mais en mieux.

---

## 📝 Exemple 1 : Le plus simple

### Version avec `if/else`

```scala
val nombre = 5

if (nombre == 1) {
  println("Un")
} else if (nombre == 2) {
  println("Deux")
} else if (nombre == 3) {
  println("Trois")
} else {
  println("Autre")
}
```

### Version avec `match` (plus propre)

```scala
val nombre = 5

nombre match {
  case 1 => println("Un")
  case 2 => println("Deux")
  case 3 => println("Trois")
  case _ => println("Autre")  // _ = "tout le reste"
}
```

**C'est exactement la même chose, mais plus lisible !**

---

## 🔍 Exemple 2 : Avec Option

### Le problème

Quand tu utilises `Option`, tu as soit `Some(valeur)` soit `None`.

### Solution avec `match`

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
- `case Some(valeur)` : Si c'est `Some`, on récupère la valeur dans `valeur`
- `case None` : Si c'est `None`, on fait autre chose

**Exemple concret** :
```scala
val touche = lireTouche()  // Retourne Option[String]

touche match {
  case Some(key) =>
    println(s"Tu as tapé : $key")
  case None =>
    println("Pas de touche")
}
```

---

## 🎮 Exemple 3 : Notre cas avec les transitions

### Le code qu'on utilise

```scala
automate.transitions.get((etatActuel, symbole)) match {
  case Some(nouvelEtat) =>
    // Transition trouvée : on va au nouvel état
    reconnaitreLoop(nouvelEtat, suite)
  case None =>
    // Pas de transition : la séquence n'est pas reconnue
    None
}
```

### Explication étape par étape

1. **`automate.transitions.get(...)`** : On cherche une transition
   - Retourne `Some(état)` si trouvé
   - Retourne `None` si pas trouvé

2. **`.match {`** : On fait un match sur le résultat

3. **`case Some(nouvelEtat) =>`** : Si on a trouvé (`Some`)
   - On récupère l'état dans `nouvelEtat`
   - On continue la récursion avec ce nouvel état

4. **`case None =>`** : Si on n'a pas trouvé (`None`)
   - On retourne `None` (pas reconnu)

---

## 🔀 Exemple 4 : Avec les listes

### Pattern matching sur les listes

```scala
val liste = List(1, 2, 3)

liste match {
  case Nil =>
    println("Liste vide")
  case premier :: reste =>
    println(s"Premier : $premier, Reste : $reste")
}
```

**Explication** :
- `case Nil` : Si la liste est vide
- `case premier :: reste` : Si la liste a au moins un élément
  - `premier` = le premier élément
  - `reste` = le reste de la liste

**Exemple concret** :
```scala
List(1, 2, 3) match {
  case Nil => println("Vide")
  case premier :: reste => 
    println(s"Premier = $premier")  // Premier = 1
    println(s"Reste = $reste")      // Reste = List(2, 3)
}
```

---

## 🎯 Exemple 5 : Dans notre code de reconnaissance

### Le code

```scala
reste match {
  case Nil =>
    // Fin de la séquence
    if (automate.etatsFinaux.contains(etatActuel)) {
      Some(etatActuel.mouvements)
    } else {
      None
    }
  case symbole :: suite =>
    // Il reste des symboles à lire
    automate.transitions.get((etatActuel, symbole)) match {
      case Some(nouvelEtat) =>
        reconnaitreLoop(nouvelEtat, suite)
      case None =>
        None
    }
}
```

### Explication étape par étape

1. **`reste match {`** : On regarde ce qui reste à lire

2. **`case Nil =>`** : Si la liste est vide
   - On a fini de lire tous les symboles
   - On vérifie si on est dans un état final

3. **`case symbole :: suite =>`** : Si la liste n'est pas vide
   - `symbole` = le premier symbole à lire
   - `suite` = le reste des symboles
   - On cherche la transition pour ce symbole
   - On continue la récursion avec le reste

---

## 📚 Plus d'exemples

### Exemple 1 : Avec des nombres

```scala
val x = 5

x match {
  case 0 => println("Zéro")
  case 1 => println("Un")
  case 2 => println("Deux")
  case _ => println("Autre nombre")
}
```

### Exemple 2 : Avec Either

```scala
val resultat: Either[String, Int] = Right(42)

resultat match {
  case Right(valeur) =>
    println(s"Succès : $valeur")
  case Left(erreur) =>
    println(s"Erreur : $erreur")
}
```

### Exemple 3 : Avec des tuples

```scala
val couple = ("Alice", 25)

couple match {
  case (nom, age) =>
    println(s"$nom a $age ans")
}
```

### Exemple 4 : Avec plusieurs cas

```scala
val nombre = 3

nombre match {
  case 1 | 2 | 3 =>
    println("Petit nombre")
  case 4 | 5 | 6 =>
    println("Moyen nombre")
  case _ =>
    println("Grand nombre")
}
```

---

## 🎓 Règles importantes

### 1. Toujours couvrir tous les cas

```scala
// ❌ Erreur : pas tous les cas couverts
val x: Option[Int] = Some(5)
x match {
  case Some(v) => println(v)
  // Pas de case None !
}

// ✅ Correct
x match {
  case Some(v) => println(v)
  case None => println("Rien")
}
```

### 2. Le `_` pour "tout le reste"

```scala
val x = 10

x match {
  case 1 => println("Un")
  case 2 => println("Deux")
  case _ => println("Autre")  // Tout le reste
}
```

### 3. L'ordre compte

```scala
// ❌ Mauvais ordre
x match {
  case _ => println("Tout")      // Ce cas sera toujours pris !
  case 1 => println("Un")        // Jamais atteint
}

// ✅ Bon ordre
x match {
  case 1 => println("Un")
  case _ => println("Tout")
}
```

---

## 💡 Astuce : Comparaison avec if/else

### Version if/else

```scala
val resultat = lireTouche()

if (resultat.isDefined) {
  val key = resultat.get
  println(s"Touche : $key")
} else {
  println("Pas de touche")
}
```

### Version match (plus sûre)

```scala
val resultat = lireTouche()

resultat match {
  case Some(key) =>
    println(s"Touche : $key")
  case None =>
    println("Pas de touche")
}
```

**Pourquoi `match` est mieux ?**
- ✅ Plus sûr : pas besoin de `.get` qui peut planter
- ✅ Plus lisible : on voit tous les cas d'un coup
- ✅ Le compilateur vérifie que tous les cas sont couverts

---

## 🎯 Résumé

### Le `match` c'est quoi ?

- Un **"si... alors... sinon"** amélioré
- Permet de **décomposer** des valeurs (Option, List, etc.)
- Le compilateur vérifie que **tous les cas** sont couverts

### Syntaxe

```scala
valeur match {
  case pattern1 => action1
  case pattern2 => action2
  case _ => actionParDefaut
}
```

### Quand l'utiliser ?

- ✅ Avec `Option` (Some/None)
- ✅ Avec `Either` (Left/Right)
- ✅ Avec les listes (Nil/head::tail)
- ✅ Pour remplacer des `if/else` compliqués

### Exemple dans notre projet

```scala
// Chercher une transition
transitions.get((etat, symbole)) match {
  case Some(nouvelEtat) => 
    // Trouvé : continuer avec nouvelEtat
  case None => 
    // Pas trouvé : retourner None
}
```

C'est tout ! 🎉

