# Explication simple : C'est quoi le `_` en Scala ?

## 🎯 Le `_` en Scala

Le `_` (underscore) en Scala, c'est un **raccourci** pour écrire des fonctions anonymes (lambdas).

---

## 📝 Exemple 1 : Le plus simple

### Version longue (sans `_`)

```scala
List(1, 2, 3).map(x => x * 2)
```

**Explication** :
- `map` : Prend chaque élément de la liste
- `x => x * 2` : Pour chaque élément `x`, multiplie par 2
- Résultat : `List(2, 4, 6)`

### Version courte (avec `_`)

```scala
List(1, 2, 3).map(_ * 2)
```

**Explication** :
- `_` = l'élément actuel (comme `x` dans la version longue)
- `_ * 2` = multiplie l'élément par 2
- Résultat : `List(2, 4, 6)`

**C'est exactement la même chose !**

---

## 🔍 Exemple 2 : Avec des chaînes

### Version longue

```scala
List("hello", "world").map(mot => mot.toUpperCase)
```

### Version courte

```scala
List("hello", "world").map(_.toUpperCase)
```

**Résultat** : `List("HELLO", "WORLD")`

**Explication** :
- `_` = chaque mot de la liste
- `_.toUpperCase` = met le mot en majuscules

---

## 🧹 Exemple 3 : Notre cas avec `trim`

### Le code qu'on utilise

```scala
Array("  [BP]  ", "  [FP]  ").map(_.trim)
```

### Version longue équivalente

```scala
Array("  [BP]  ", "  [FP]  ").map(element => element.trim)
```

**Résultat** : `Array("[BP]", "[FP]")`

**Explication étape par étape** :

1. On a un tableau : `Array("  [BP]  ", "  [FP]  ")`
2. `map` prend chaque élément :
   - Premier élément : `"  [BP]  "`
   - Deuxième élément : `"  [FP]  "`
3. `_.trim` enlève les espaces :
   - `"  [BP]  ".trim` → `"[BP]"`
   - `"  [FP]  ".trim` → `"[FP]"`
4. Résultat : `Array("[BP]", "[FP]")`

---

## 🎯 Règle simple

**Le `_` remplace le paramètre d'une fonction anonyme.**

### Syntaxe générale

```scala
// Version longue
liste.map(x => fonction(x))

// Version courte
liste.map(_.fonction)
```

### Quand utiliser `_` ?

Tu peux utiliser `_` quand :
- Tu n'utilises le paramètre qu'**une seule fois**
- Tu appelles juste **une méthode** sur le paramètre

### Quand NE PAS utiliser `_` ?

Quand tu as besoin du paramètre plusieurs fois :

```scala
// ❌ Ça ne marche pas
List(1, 2, 3).map(_ + _)  // Erreur ! Lequel est lequel ?

// ✅ Il faut être explicite
List(1, 2, 3).map(x => x + x)  // x + x = double chaque nombre
```

---

## 📚 Plus d'exemples

### Exemple 1 : Multiplier par 2

```scala
// Long
List(1, 2, 3).map(nombre => nombre * 2)

// Court
List(1, 2, 3).map(_ * 2)
```

### Exemple 2 : Mettre en majuscules

```scala
// Long
List("a", "b", "c").map(lettre => lettre.toUpperCase)

// Court
List("a", "b", "c").map(_.toUpperCase)
```

### Exemple 3 : Enlever les espaces

```scala
// Long
Array("  hello  ", "  world  ").map(texte => texte.trim)

// Court
Array("  hello  ", "  world  ").map(_.trim)
```

### Exemple 4 : Prendre la longueur

```scala
// Long
List("hello", "world").map(mot => mot.length)

// Court
List("hello", "world").map(_.length)
```

### Exemple 5 : Avec plusieurs paramètres

```scala
// Si tu as besoin de 2 paramètres
List(1, 2, 3).zip(List(10, 20, 30)).map { case (a, b) => a + b }

// Ou avec _
List(1, 2, 3).zip(List(10, 20, 30)).map(_ + _)  // ❌ Erreur !

// Il faut être explicite
List(1, 2, 3).zip(List(10, 20, 30)).map { case (a, b) => a + b }
// Résultat : List(11, 22, 33)
```

---

## 🎓 Résumé

### Le `_` c'est quoi ?

- Un **raccourci** pour écrire des fonctions anonymes
- Il représente **l'élément actuel** dans une fonction `map`, `filter`, etc.

### Syntaxe

```scala
// Long
liste.map(x => x.methode)

// Court
liste.map(_.methode)
```

### Quand l'utiliser ?

- ✅ Quand tu appelles **une seule méthode** sur l'élément
- ✅ Quand tu utilises l'élément **une seule fois**

### Quand ne pas l'utiliser ?

- ❌ Quand tu as besoin de l'élément **plusieurs fois**
- ❌ Quand tu as besoin de **plusieurs paramètres**

---

## 💡 Astuce

Si tu ne comprends pas `_.trim`, pense à :
1. `_` = "chaque élément de la liste"
2. `.trim` = "enlève les espaces"
3. `_.trim` = "pour chaque élément, enlève les espaces"

C'est tout ! 🎉

