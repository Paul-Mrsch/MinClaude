# Architecture de MinClaude

## Vue d'ensemble

```
src/main/java/minclaude/
├── MinClaudeMod.java        Point d'entrée : branche les événements, le contenu, l'UI, le raccourci
├── ModSettings.java         Options (Paramètres > MinClaude)
├── stats/                   Logique PURE, sans import Mindustry ni Arc : testable avec JUnit seul
│   ├── RingSeries           tampon circulaire de floats
│   ├── CompactingSeries     série de toute la partie, fusionne ses points deux à deux quand elle est pleine
│   ├── TieredSeries         3 résolutions : 1 s (10 min), 10 s (1 h), 60 s+ (toute la partie)
│   ├── MetricHistory        clé texte -> TieredSeries, + secondes écoulées
│   ├── DeltaAccumulator     estime entrées/sorties à partir des variations de stock à chaque tick
│   ├── Trend                pente par moindres carrés, échéance avant d'atteindre une cible
│   ├── ResourceStats        résumé d'une ressource (stock, débits, tendance, échéances)
│   ├── AlertEngine          règles d'alerte avec anti-répétition
│   ├── HistoryCodec         format binaire versionné de l'historique
│   ├── Contrast             lisibilité des couleurs sur fond sombre (WCAG)
│   ├── TimeRange, SeriesView, Format
├── logic/                   Logique PURE de jeu (testable sans le jeu)
│   ├── IndustryStatus       classement d'une usine (active, sans entrée, sans énergie, sortie pleine, désactivée)
│   ├── IndustryReport       synthèse par type d'usine + goulots (ressources qui bloquent le plus d'usines)
│   ├── PowerAggregator      somme des réseaux électriques, chacun compté une fois
│   ├── DefenseReport        tourelles, unités, prochaine vague
│   ├── TargetScorer         note des cibles de l'IA ennemie + profil par difficulté
│   ├── OreScatter           répartition déterministe des gisements (bruit de valeur)
│   └── EnemyWavePlan        vagues des ennemis du mod selon la difficulté
├── tracking/                Liaison avec le jeu
│   ├── ResourceTracker      observe le noyau, alimente l'historique, orchestre scanner et alertes
│   ├── BaseScanner          relevé de la base chaque seconde (énergie historisée, usines, défense, vague)
│   └── HistoryChunk         chunk de sauvegarde « minclaude-history »
├── debug/SelfTest.java      autotest de l'UI dans le vrai client (actif seulement avec MINCLAUDE_SELFTEST)
├── ai/                      IA ennemie
│   ├── SmartGroundAI        GroundAI + ciblage intelligent (repli vanilla si désactivée ou bloquée)
│   └── SmartAI              installation sur les unités terrestres, profil lu dans les options
├── world/WorldSetup.java    nouvelle partie : gisements + vagues ennemies (une seule fois, marqueur dans les règles)
├── ui/                      Interface (Arc scene2d)
│   ├── LineGraph            graphique en courbes (couleurs rendues lisibles)
│   ├── ResourceOverlay      mini-panneau permanent du HUD
│   ├── DashboardDialog      dashboard plein écran, gère les onglets
│   └── tabs/                DashboardTab (base), ResourcesTab, PowerTab, IndustryTab, DefenseTab
└── content/                 Contenu du jeu
    ├── MCItems, MCBlocks, MCUnits  déclarations (listes `all`, `ores`, `allies`, `enemies` utilisées par les tests)
    └── MCTechTree           branchement dans le tech tree de Serpulo
src/tools/java/              Générateur de sprites (hors jar)
src/test/java/               Tests unitaires (logique pure + fichiers du mod)
src/integrationTest/java/    Tests d'intégration headless (vrai jeu)
assets/                      bundles (traductions), sprites -> copiés à la racine du jar
```

**Règle de dépendance** : `stats` et `logic` ne dépendent de rien. `tracking` et `ui` dépendent de `stats` et de Mindustry. `content` ne dépend que de Mindustry. Mettre dans `stats` toute logique qui peut s'y trouver : c'est la partie la mieux testée.

## Flux de données du suivi des ressources

```
Trigger.update (chaque frame, ~60 fois/s)
   └─ si state.isPlaying() :
        pour chaque ressource du jeu :
            stock = noyau.items.get(item)
            si stock > 0 -> ressource suivie à partir de maintenant
            DeltaAccumulator.observe(id, stock)        (hausse = entrée, baisse = sortie)
        timer += Time.delta  (en ticks de jeu, donc la pause et la vitesse du jeu sont respectées)
        à chaque 60 ticks :
            MetricHistory.record("item/<nom>/stock" | "/in" | "/out", valeur)
            MetricHistory.tick()
            AlertEngine.evaluate(...) -> toast dans le HUD
```

- Le temps est compté en **ticks de jeu**, pas en temps réel : une partie en pause n'enregistre rien.
- Les entrées et sorties sont une **estimation** : une entrée et une sortie dans le même tick se compensent. Le stock et la tendance nette, eux, sont exacts.
- Au chargement d'une carte (`WorldLoadEvent`) ou à la réinitialisation (`ResetEvent`), l'historique repart de zéro.

## Persistance dans la sauvegarde

- Le chunk est enregistré par `SaveVersion.addCustomChunk("minclaude-history", …)` dans le constructeur du mod.
- **Ordre de lecture vérifié dans le code du jeu** (`SaveVersion.read`) : meta → contenu → **carte (déclenche `WorldLoadEvent`, qui efface l'historique)** → entités → marqueurs → **chunks personnalisés (qui restaurent l'historique)**. L'historique chargé n'est donc pas effacé. C'est vérifié par `TrackingIT.historySurvivesSaveAndLoad`.
- La lecture commence par consommer exactement les `length` octets du chunk, puis les décode. Un historique corrompu ou d'une version future est ignoré (journal d'erreur, historique vide) **sans jamais empêcher le chargement de la partie**.
- `writeNet() = false` : l'historique n'est pas envoyé aux clients multijoueur, ce qui est inutile pour un mod solo.

### Format `HistoryCodec` v1 (big-endian, `DataOutput`)

```
int    MAGIC = 0x4D434831 ("MCH1")
int    VERSION = 1
long   secondes écoulées
int    nombre de clés
répété : UTF clé, puis TieredSeries :
    long  échantillons
    fine   : int n, n × float
    medium : int n, n × float ; float acc ; int count
    coarse : int step ; float acc ; int count ; int n ; n × float
```

Taille maximale par métrique : environ 7 Ko (600 + 360 + 720 floats). Une partie qui suit 20 ressources (60 métriques) pèse donc au plus environ 400 Ko avant la compression appliquée par le jeu.

## Interface

- **Mini-panneau** (`ResourceOverlay`) : ajouté à `ui.hudGroup` à droite de l'écran, rafraîchi deux fois par seconde. Il trie les ressources par échéance d'épuisement, puis par baisse la plus forte, et masque ses lignes quand il est replié.
- **Dashboard** (`DashboardDialog`) : rafraîchi chaque seconde quand il est ouvert. Les entrées et sorties sont stockées par seconde et affichées par minute.
- **Raccourci** : `KeyBind.add("minclaude-dashboard", KeyCode.k, "minclaude")`, modifiable dans Paramètres > Commandes. Il est ignoré pendant la saisie de texte.

## Relevé de la base (V1)

Une fois par seconde de jeu, après l'enregistrement des ressources, `BaseScanner.scan(équipe, historique)` parcourt `team.data().buildings` et `team.data().units` :

- **Énergie** : pour chaque bâtiment relié à un réseau, `PowerAggregator.add(graph.getID(), …)` ne compte chaque réseau qu'une fois. Les valeurs du jeu sont par tick et converties par seconde. Les clés `power/produced`, `power/consumed`, `power/stored`, `power/capacity` et `power/satisfaction` sont historisées comme les ressources, donc sauvegardées.
- **Usines** (`GenericCrafter`, `Separator`, `Drill`) : `IndustryStatus.classify(enabled, shouldConsume, efficiency, consomme de l'énergie, power.status, ressource manquante)`. Les ressources manquantes sont celles des `ConsumeItems` absentes du stock interne. Une foreuse hors gisement (`dominantItems == 0`) est « sans entrée ».
- **Défense** : `TurretBuild.hasAmmo()`, `healthf()` ; prochaine vague = `SpawnGroup.getSpawned(state.wave - 1)` multiplié par le nombre de points d'apparition.
- **Alertes** : `AlertEngine.evaluateCondition` pour les usines privées d'une ressource, la couverture de l'énergie sous 75 % et les tourelles à sec.

## Nouvelle partie (V1)

`WorldSetup` écoute `Trigger.newGame`, déclenché par le jeu une fois les règles d'une nouvelle carte ou d'un nouveau secteur en place. Si le marqueur `minclaude-setup` est absent de `state.rules.tags` (enregistrées dans la sauvegarde), il :

1. pose les gisements avec `OreScatter`. La graine vient du secteur ou du nom de la carte. Les cases éligibles sont un sol praticable, sans bloc, sans minerai et sans liquide. Il recharge ensuite le rendu du sol et la minimap ;
2. ajoute à `state.rules.spawns` les `SpawnGroup` d'`EnemyWavePlan.forDifficulty(difficulté)`.

## IA ennemie (V1)

`SmartAI.install()` (dans `Mod.init`, après le chargement du contenu) remplace `aiController` par `SmartGroundAI` pour chaque type d'unité terrestre dont l'IA par défaut est exactement `GroundAI`. Les mineurs et les unités spéciales gardent la leur. Les unités du joueur sont pilotées par `CommandAI` et ne sont pas concernées.

`SmartGroundAI`, toutes les 45 ticks : elle note les bâtiments ennemis dans le rayon de recherche (`Units.nearbyBuildings`) avec `TargetScorer.score(type, distance, PV, à portée)`. Elle s'approche de la meilleure cible et la désigne comme cible de tir (`findMainTarget`). Si elle reste bloquée 2 s, elle abandonne pendant 5 s et reprend `GroundAI.updateMovement` (chemin vers le noyau). Avec un profil désactivé (Facile ou option décochée), elle se comporte comme `GroundAI`.

## Points d'extension prévus

- **Tactiques de groupe (V2)** : coordination entre `SmartGroundAI` d'une même vague (point de ralliement, flanquement), avec une logique pure dans `logic/`.
- **Adaptation (V3)** : `EnemyWavePlan` recevra un résumé de la défense du joueur (via `DefenseReport`) pour ajuster la composition des vagues.
- **Difficulté (V2)** : PV et dégâts des ennemis selon `ModSettings.difficulty()`.
