# Explication simple : Les états de l'automate

## 🎯 C'est quoi un état ?

Un état, c'est comme une **position** ou une **case** dans un jeu.

Imagine que tu es dans un labyrinthe :
- Tu commences à l'entrée (état 0)
- Tu marches dans les couloirs (états intermédiaires)
- Tu arrives à une sortie (état final) → Tu as gagné !

---

## 📍 Exemple 1 : Le plus simple possible

### Le problème

Tu veux reconnaître le combo `[BP]` qui correspond au mouvement "Punch".

### Comment on fait ?

On crée **2 états** :

```
État 0 (départ)  →  État 1 (arrivée)
```

**Explication** :
- **État 0** : Tu es au départ, tu n'as encore rien lu
- **État 1** : Tu as lu `[BP]`, tu es arrivé !

### Visuellement

```
Tu es ici →  [0]  --[BP]-->  [1] ✅
             départ          arrivée
```

### Code

```scala
// État 0 : Le départ
val etat0 = Etat(
  id = 0,
  estFinal = false,        // Pas encore arrivé
  mouvements = Set()       // Aucun mouvement reconnu
)

// État 1 : L'arrivée
val etat1 = Etat(
  id = 1,
  estFinal = true,         // ✅ C'est un état final !
  mouvements = Set("Punch") // On reconnaît "Punch" ici
)
```

### Comment ça marche ?

1. Tu commences à l'**état 0**
2. Tu lis le symbole `[BP]`
3. Tu suis la flèche `--[BP]-->`
4. Tu arrives à l'**état 1**
5. L'état 1 est **final** → Tu as reconnu "Punch" ! ✅

---

## 🔀 Exemple 2 : Avec 2 symboles

### Le problème

Tu veux reconnaître le combo `[BP], [FP]` qui correspond à "Combo".

### Comment on fait ?

On crée **3 états** :

```
État 0  --[BP]-->  État 1  --[FP]-->  État 2 ✅
départ             milieu             arrivée
```

**Explication** :
- **État 0** : Tu es au départ
- **État 1** : Tu as lu `[BP]`, tu es au milieu
- **État 2** : Tu as lu `[BP], [FP]`, tu es arrivé !

### Visuellement

```
[0]  --[BP]-->  [1]  --[FP]-->  [2] ✅
```

### Code

```scala
// État 0 : Le départ
val etat0 = Etat(0, false, Set())

// État 1 : Après avoir lu [BP]
val etat1 = Etat(1, false, Set())  // Pas encore arrivé

// État 2 : Après avoir lu [BP], [FP]
val etat2 = Etat(2, true, Set("Combo"))  // ✅ Arrivé !
```

### Comment ça marche ?

1. Tu commences à l'**état 0**
2. Tu lis `[BP]` → Tu vas à l'**état 1**
3. Tu lis `[FP]` → Tu vas à l'**état 2**
4. L'état 2 est **final** → Tu as reconnu "Combo" ! ✅

---

## 🎮 Exemple 3 : Plusieurs combos

### Le problème

Tu veux reconnaître :
- `[BP]` → "Punch"
- `[BP], [FP]` → "Combo"

### Comment on fait ?

On **réutilise** les états ! On ne crée pas deux chemins séparés.

```
État 0  --[BP]-->  État 1 ✅  --[FP]-->  État 2 ✅
départ             (Punch)               (Combo)
```

**Explication** :
- Les deux combos commencent par `[BP]`
- Donc ils partagent le même chemin jusqu'à l'état 1
- L'état 1 est final (reconnaît "Punch")
- Si tu continues avec `[FP]`, tu vas à l'état 2 (reconnaît "Combo")

### Visuellement

```
[0]  --[BP]-->  [1] ✅  --[FP]-->  [2] ✅
     (Punch)              (Combo)
```

### Code

```scala
// État 0 : Le départ
val etat0 = Etat(0, false, Set())

// État 1 : Après [BP] → Reconnaît "Punch"
val etat1 = Etat(1, true, Set("Punch"))  // ✅ Final

// État 2 : Après [BP], [FP] → Reconnaît "Combo"
val etat2 = Etat(2, true, Set("Combo"))  // ✅ Final
```

### Comment ça marche ?

**Scénario 1** : Tu tapes `[BP]`
1. Tu es à l'état 0
2. Tu lis `[BP]` → Tu vas à l'état 1
3. L'état 1 est final → "Punch" reconnu ! ✅

**Scénario 2** : Tu tapes `[BP], [FP]`
1. Tu es à l'état 0
2. Tu lis `[BP]` → Tu vas à l'état 1
3. Tu lis `[FP]` → Tu vas à l'état 2
4. L'état 2 est final → "Combo" reconnu ! ✅

---

## 🏗️ Comment on construit les états ?

### Étape par étape

**Règle 1** : `"Punch": [BP]`

1. On commence à l'état 0
2. On lit `[BP]`
3. Pas de transition `(0, "[BP]")` → On crée l'état 1
4. On crée la transition : `(0, "[BP]") -> 1`
5. L'état 1 devient final avec "Punch"

```
[0]  --[BP]-->  [1] ✅ (Punch)
```

**Règle 2** : `"Combo": [BP], [FP]`

1. On commence à l'état 0
2. On lit `[BP]`
3. La transition `(0, "[BP]")` existe déjà → On va à l'état 1
4. On lit `[FP]`
5. Pas de transition `(1, "[FP]")` → On crée l'état 2
6. On crée la transition : `(1, "[FP]") -> 2`
7. L'état 2 devient final avec "Combo"

```
[0]  --[BP]-->  [1] ✅  --[FP]-->  [2] ✅
     (Punch)              (Combo)
```

---

## 🔍 Comment on utilise les états ?

### Algorithme simple

```scala
def reconnaitre(automate: Automate, sequence: List[String]): Option[Set[String]] = {
  // 1. On commence à l'état initial (toujours 0)
  var etatActuel = automate.etatInitial
  
  // 2. Pour chaque symbole de la séquence
  for (symbole <- sequence) {
    // 3. Chercher la transition depuis l'état actuel avec ce symbole
    val transition = automate.transitions.get((etatActuel, symbole))
    
    transition match {
      case Some(nouvelEtat) =>
        // Transition trouvée : on va au nouvel état
        etatActuel = nouvelEtat
      case None =>
        // Pas de transition : la séquence n'est pas reconnue
        return None
    }
  }
  
  // 4. On a fini de lire la séquence
  // Vérifier si on est dans un état final
  if (automate.etatsFinaux.contains(etatActuel)) {
    // ✅ Reconnu ! Retourner les mouvements
    Some(etatActuel.mouvements)
  } else {
    // ❌ Non reconnu
    None
  }
}
```

### Exemple concret

**Séquence** : `List("[BP]")`
**Automate** : 
```
[0]  --[BP]-->  [1] ✅ (Punch)
```

**Étapes** :

1. `etatActuel = État 0`
2. Symbole `"[BP]"` :
   - Chercher transition `(0, "[BP]")` → Trouvé ! → `État 1`
   - `etatActuel = État 1`
3. Fin de la séquence
4. État 1 est final ? Oui ! → Retourne `Some(Set("Punch"))` ✅

---

## 🎯 Résumé ultra-simple

### Qu'est-ce qu'un état ?

Un état, c'est une **position** dans l'automate.

### Les 3 types d'états

1. **État initial** (0) : Le point de départ
2. **États intermédiaires** : Les cases du milieu
3. **États finaux** : Les cases d'arrivée (où on reconnaît les mouvements)

### Comment ça marche ?

```
[0]  --symbole-->  [1]  --symbole-->  [2] ✅
départ             milieu             arrivée
```

1. Tu commences à l'état 0
2. Tu lis un symbole
3. Tu suis la flèche (transition)
4. Tu arrives à un nouvel état
5. Si c'est un état final → Tu as reconnu un mouvement ! ✅

### Exemple visuel complet

```
Automate pour reconnaître "[BP]" et "[BP], [FP]"

        [BP]
[0] ──────────► [1] ✅ (Punch, Jab, etc.)
        │
        │ [FP]
        ▼
        [2] ✅ (Combo)
```

**Si tu tapes `[BP]`** :
- Tu vas de 0 → 1
- État 1 est final → "Punch" reconnu ! ✅

**Si tu tapes `[BP], [FP]`** :
- Tu vas de 0 → 1 → 2
- État 2 est final → "Combo" reconnu ! ✅

---

## 💡 Analogie simple

Imagine que tu es dans une ville :

- **Les états** = Les intersections de rues
- **Les transitions** = Les rues (avec des panneaux)
- **L'état initial** = Ta maison (point de départ)
- **Les états finaux** = Les magasins où tu veux aller (destinations)

**Exemple** :
```
Ta maison [0]  --rue A-->  Intersection [1]  --rue B-->  Magasin [2] ✅
```

1. Tu pars de ta maison (état 0)
2. Tu prends la rue A → Tu arrives à l'intersection (état 1)
3. Tu prends la rue B → Tu arrives au magasin (état 2) ✅

C'est exactement comme ça que fonctionne un automate !

---

## ❓ Questions fréquentes

### Q: Pourquoi on a besoin de plusieurs états ?

**R:** Parce que chaque symbole te fait avancer d'un état. Si tu as `[BP], [FP]`, tu as besoin de 3 états :
- État 0 : Rien lu
- État 1 : `[BP]` lu
- État 2 : `[BP], [FP]` lu

### Q: C'est quoi un état final ?

**R:** Un état final, c'est un état où on **reconnaît un mouvement**. Quand tu arrives à un état final, tu as gagné ! ✅

### Q: Pourquoi on réutilise les états ?

**R:** Pour économiser de l'espace. Si deux combos commencent par `[BP]`, ils partagent le même chemin jusqu'à l'état après `[BP]`.

### Q: Comment on sait si on est dans un état final ?

**R:** L'état a un champ `estFinal = true`. On vérifie juste ça !

---

## 🎓 Conclusion

Un état, c'est juste une **case** dans l'automate :
- Tu commences à la case 0
- Tu te déplaces de case en case en lisant les symboles
- Si tu arrives à une case finale → Tu as reconnu un mouvement ! ✅

C'est tout ! 🎉

