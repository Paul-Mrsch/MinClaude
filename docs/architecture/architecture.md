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
├── tracking/                Liaison avec le jeu
│   ├── ResourceTracker      observe le noyau, alimente l'historique, déclenche les alertes
│   └── HistoryChunk         chunk de sauvegarde « minclaude-history »
├── debug/SelfTest.java      autotest de l'UI dans le vrai client (actif seulement avec MINCLAUDE_SELFTEST)
├── ui/                      Interface (Arc scene2d)
│   ├── LineGraph            graphique en courbes
│   ├── ResourceOverlay      mini-panneau permanent du HUD
│   └── DashboardDialog      dashboard plein écran
└── content/                 Contenu du jeu
    ├── MCItems, MCBlocks    déclarations (listes `all` utilisées par les tests)
    └── MCTechTree           branchement dans le tech tree de Serpulo
src/tools/java/              Générateur de sprites (hors jar)
src/test/java/               Tests unitaires (logique pure + fichiers du mod)
src/integrationTest/java/    Tests d'intégration headless (vrai jeu)
assets/                      bundles (traductions), sprites -> copiés à la racine du jar
```

**Règle de dépendance** : `stats` ne dépend de rien. `tracking` et `ui` dépendent de `stats` et de Mindustry. `content` ne dépend que de Mindustry. Mettre dans `stats` toute logique qui peut s'y trouver : c'est la partie la mieux testée.

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

## Points d'extension prévus

- **Énergie (V1)** : de nouvelles clés `power/produced`, `power/consumed`, `power/stored` dans le même `MetricHistory`. Le format et l'UI de graphique sont réutilisés tels quels.
- **IA (V1+)** : paquet `ai/` avec un score de cible pur (testable) dans `stats` ou `ai/logic`, et des contrôleurs (`AIController`) assignés aux `UnitType` ennemis.
- **Difficulté** : la valeur `minclaude-difficulty` est déjà enregistrée dans les options. Elle sera lue à partir de la V2.
