# TP2 — GLO-3004 : Publisher-Subscriber

**Cours :** Spécification et vérification formelle de logiciels  
**Session :** Hiver 2026  
**Langage :** Java 21  
**Remise :** 28 mars à 23h50

---

## Structure du projet

```
tp2/
├── src/
│   ├── IBroker.java            ← interface du broker (ne pas modifier)
│   ├── IPublisher.java         ← interface publisher (ne pas modifier)
│   ├── ISubscriber.java        ← interface subscriber (ne pas modifier)
│   ├── TraceLogger.java        ← logger synchronisé partagé (ne pas modifier)
│   ├── Broker.java             ← implémentation du broker
│   ├── PublisherThread.java    ← thread publisher
│   ├── SubscriberThread.java   ← thread subscriber
│   └── Main.java               ← point d'entrée, orchestration
├── test/
│   ├── StubBroker.java         ← broker factice pour tester sans le vrai broker
│   └── BrokerTest.java         ← tests manuels pour valider Broker.java
├── MANIFEST.MF                 ← requis pour le jar exécutable
├── Makefile                    ← raccourcis de compilation (optionnel)
└── LisezMoi.txt                ← documentation de remise
```

---

## Compilation et exécution

### Option 1 — commandes Java directement

```bash
# 1. Compiler
javac -d out src/*.java

# 2. Créer le jar
jar cfm tp2.jar MANIFEST.MF -C out .

# 3. Lancer
java -Dn=2 -Dp=2 -Ds=3 -Dt=100 -jar tp2.jar
```

### Option 2 — avec Make

```bash
make        # compile + construit le jar
make run    # lance avec les paramètres par défaut (n=2 p=2 s=3 t=100)
make test   # compile et lance BrokerTest
make clean  # supprime out/ et tp2.jar
```

### Commande de correction (examen)

```bash
java -Dn=2 -Dp=2 -Ds=3 -Dt=100 -jar tp2.jar
```

> **Note :** les valeurs des paramètres peuvent changer lors de la correction.  
> `-20 points` si le jar ne s'exécute pas avec cette commande.

---

## Paramètres

| Paramètre | Description | Défaut |
|-----------|-------------|--------|
| `-Dn`     | Taille max du buffer broker + max publishers simultanés (N dans la spéc) | `2` |
| `-Dp`     | Nombre de publishers par app | `2` |
| `-Ds`     | Nombre de subscribers par app | `3` |
| `-Dt`     | Durée d'exécution en millisecondes | `100` |

Les paramètres sont lus via `System.getProperty()` — ils se passent avec `-D` avant `-jar`.

---

## C'est quoi un jar ?

Un `.jar` est un zip de fichiers `.class` (bytecode Java compilé). La JVM sait quelle classe lancer en premier grâce au fichier `MANIFEST.MF` qui contient :

```
Main-Class: Main
```

Sans ce fichier, `java -jar` ne sait pas par où commencer et plante. C'est pourquoi il est obligatoire.

---

## Format de la trace

Chaque action affiche exactement une ligne :

```
app.type.id ACTION
```

- `app` : `i` ou `t`
- `type` : `publisher` ou `subscriber`
- `id` : numéro du thread (`1..NB_P` ou `1..NB_S`)

### Actions possibles

| Processus | Action | Moment |
|-----------|--------|--------|
| Publisher | `SUPPLY` | début du cycle, avant de se connecter |
| Publisher | `CONNECT_PUB` | connexion au broker |
| Publisher | `PUB` | dépôt du message dans le buffer |
| Publisher | `CLOSE_PUB` | fin du cycle publisher |
| Subscriber | `CONNECT_SUB` | connexion au broker |
| Subscriber | `SUB` | retrait du message du buffer |
| Subscriber | `CLOSE_SUB` | fin du cycle subscriber |
| Subscriber | `CONSUME` | consommation du message |

### Exemple de trace valide

```
i.publisher.2 SUPPLY
i.publisher.2 CONNECT_PUB
i.publisher.2 PUB
i.publisher.2 CLOSE_PUB
i.subscriber.1 CONNECT_SUB
i.subscriber.1 SUB
i.subscriber.1 CLOSE_SUB
i.subscriber.1 CONSUME
t.publisher.1 SUPPLY
t.publisher.1 CONNECT_PUB
...
```

---

## Correspondance FSP → Java

| Processus FSP | Classe Java | Notes |
|---------------|-------------|-------|
| `PUB3` | `PublisherThread` | Boucle infinie : `SUPPLY → connectPub → pub → CLOSE_PUB` |
| `SUB3` | `SubscriberThread` | Boucle infinie : `connectSub → sub → CLOSE_SUB → CONSUME` |
| `BROKER4` | `Broker` | Buffer borné N, `ReentrantLock` + 3 `Condition` |
| `CONTROLLER_SUB_SAME_ORIGIN_PUB` | `Broker` (interne) | Condition `pubSlot` — bloque si `connected >= N` |
| `FORBIDDEN` / `FORBIDDEN_VALIDATOR` | *(non implémenté)* | Voir section ci-dessous |
| `queue` / `dequeue` | *(non implémenté)* | Voir section ci-dessous |
| `SYSTEM11` | `Main` | Crée 2 instances (i et t), lance les threads, timer `-Dt` |

---

## Processus et actions non implémentés

Le professeur confirme qu'il n'est pas obligatoire d'implémenter tous les processus, à condition d'expliquer comment la spécification est respectée.

### FORBIDDEN / FORBIDDEN_VALIDATOR

Non implémentés explicitement. La contrainte est garantie **structurellement par Java** :

- `PublisherThread` reçoit un `IBroker` et n'appelle que `connectPub()` et `pub()` — il est **impossible** à la compilation qu'il appelle `connectSub()` ou `sub()`.
- `SubscriberThread` reçoit le même `IBroker` et n'appelle que `connectSub()` et `sub()`.

C'est une garantie plus forte qu'un `STOP` FSP — le compilateur la vérifie statiquement.

### queue / dequeue

Non implémentés comme actions distinctes. Leur rôle est absorbé directement dans `pub()` et `sub()` du `Broker` :

- `count++` dans `pub()` joue le rôle de `queue`
- `count--` dans `sub()` joue le rôle de `dequeue`

Le comportement observable (la trace) est identique à la spécification FSP.

---

## Architecture du Broker

Le `Broker` utilise un `ReentrantLock` avec 3 `Condition` :

```
count     : nb de messages dans le buffer    (0 <= count <= N)
connected : nb de publishers connectés        (0 <= connected <= N)

pubSlot   : attend si connected >= N   (CONTROLLER_SUB_SAME_ORIGIN_PUB)
notEmpty  : attend si count == 0       (subscriber bloque si buffer vide)
```

Séquence publisher :
```
connectPub() → bloque si connected >= N → connected++
pub()        → count++, connected--, signal pubSlot + notEmpty
```

Séquence subscriber :
```
connectSub() → bloque si count == 0
sub()        → count--
```

---

## Tests

### Tester le Broker seul (avant intégration)

```bash
javac -d out src/TraceLogger.java src/IBroker.java src/Broker.java test/BrokerTest.java
java -cp out BrokerTest
```

Trois tests :
1. Flux normal pub → sub
2. Subscriber bloque si buffer vide
3. Jamais plus de N publishers connectés simultanément

### Tester Publisher/Subscriber sans le vrai Broker

Utiliser `StubBroker` qui retourne immédiatement sans bloquer :

```bash
javac -d out src/*.java test/StubBroker.java
# puis instancier StubBroker dans un main de test
```

### Checklist d'intégration finale

Lancer et vérifier visuellement :

```bash
java -Dn=2 -Dp=2 -Ds=3 -Dt=100 -jar tp2.jar | head -60
```

- [ ] Chaque ligne suit le format `app.type.id ACTION`
- [ ] `SUPPLY` apparaît avant `CONNECT_PUB` pour chaque publisher
- [ ] Jamais plus de N `CONNECT_PUB` sans `PUB` entre eux
- [ ] Les labels `i.*` et `t.*` apparaissent tous les deux
- [ ] Le programme termine proprement après ~Dt ms

---

## Répartition des tâches

| Fichier | Responsable |
|---------|-------------|
| `IBroker.java`, `IPublisher.java`, `ISubscriber.java`, `TraceLogger.java` | Fares |
| `Broker.java` | Équipier 2 |
| `PublisherThread.java`, `SubscriberThread.java` | Équipier 3 |
| `Main.java`, `LisezMoi.txt` | Équipier 4 |
