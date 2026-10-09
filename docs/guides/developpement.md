# Guide du développeur

## Prérequis

- JDK 17 ou plus récent (testé avec JDK 24). Gradle est fourni par le wrapper (`./gradlew`), rien d'autre à installer.
- Mindustry installé dans `/Applications/Mindustry.app` (version ≥ 160.4) pour tester en jeu.
- Au premier build, Gradle télécharge `dependencies.jar` et `assets.jar` de la release Mindustry v160.5 depuis GitHub.

## Commandes

| Commande | Effet |
|---|---|
| `./gradlew test` | Tests unitaires (quelques secondes) |
| `./gradlew integrationTest` | Tests d'intégration : démarre Mindustry en headless |
| `./gradlew check` | Les deux |
| `./gradlew jar` | Produit `build/libs/MinClaude.jar` |
| `./gradlew deployLocal` | `check` puis copie le jar dans `~/Library/Application Support/Mindustry/mods/` |
| `./gradlew play` | `deployLocal` puis lance Mindustry |
| `./gradlew selfTest` | Autotest dans le vrai client : captures et rapport dans `build/selftest/out/` |
| `./gradlew selfTestCompat` | Même autotest avec les autres mods installés du joueur (compatibilité), dans `build/selftestCompat/out/` |
| `./gradlew generateSprites` | Régénère `assets/sprites/**` et `icon.png` |
| `./gradlew spriteSheet` | Planche de tous les sprites dans `docs/images/sprites.png`, pour les relire |

Le journal du jeu se trouve dans `~/Library/Application Support/Mindustry/last_log.txt`. Les lignes du mod commencent par `[MinClaude]`.

## Ajouter du contenu (checklist)

Exemple : une nouvelle ressource `nickel`.

1. **Déclaration** : dans `MCItems.load()`, `nickel = add(new Item("nickel", Color.valueOf("…")){{ … }});`. Le préfixe `minclaude-` est ajouté automatiquement.
2. **Tech tree** : une ligne dans `MCTechTree.load()`, sous un nœud vanilla ou du mod.
3. **Sprite** : une ligne `Spec` dans `SpriteGenerator.SPECS` (type : `GEM`, `INGOT`, `ORE`, `WALL`, `CRAFTER`, `WASHER`, `DRILL`, `CONVEYOR`, `TURRET`, `NODE`, `MECH`, `FLYER`, `WEAPON`), puis `./gradlew generateSprites spriteSheet` pour relire le résultat. Le nom du fichier est le nom interne **sans** préfixe : `assets/sprites/items/nickel.png`.
4. **Traductions** : `item.minclaude-nickel.name` et `.description` dans `bundle.properties` **et** `bundle_fr.properties`.
5. **Tests** : `./gradlew check`. `ContentIT` et `AssetsTest` échouent si le sprite, la taille du sprite, une traduction ou le nœud du tech tree manque.
6. **Doc** : mettre à jour `Etat.md`, `ToDo.md` et le tableau du contenu dans le guide joueur.

Préfixes des clés de traduction : `item.`, `block.`, `liquid.`, `unit.`, suivis de `minclaude-<nom>`.

### Style graphique

Le générateur suit le style des mods récents (Exogenesis, entre autres) sans reprendre leurs images :

- **aplats par facette**, sans dégradé : 6 tons par matière, donnés par `ramp(couleur)`. Les ombres glissent vers le bleu-violet et se saturent, les lumières glissent vers le jaune et se désaturent ;
- **angles coupés à 45°** (`Canvas.chamfer`, `octagon`) partout : cadres, plaques, creusets, noyaux ;
- **biseaux** (`Canvas.bevel`) : bord haut-gauche éclairé, bord bas-droit dans l'ombre (lumière du jeu en haut à gauche) ;
- **métal gris-bleu** (`M[0..5]`) pour les châssis, **bandes de couleur vive** de la matière (équerres de coin, bandes en X, plaques latérales) ;
- **motifs symétriques** : anneaux concentriques, rainures d'usinage, boulons octogonaux ;
- **contour** sombre de 1 px sur les blocs et objets. Pour les unités, le jeu génère le contour lui-même.

Pour relire le résultat : `./gradlew generateSprites spriteSheet`, puis ouvrir `docs/images/sprites.png`. Pour juger dans le jeu : `./gradlew selfTest`, puis ouvrir la capture `13-base-demo.png`.

### Régions de sprites demandées par le jeu

`ContentIT.requiredRegions` liste les noms de fichiers attendus. Le test échoue s'il en manque un.

| Contenu | Fichiers (sans préfixe) |
|---|---|
| Ressource, bloc simple, tourelle, nœud | `<nom>` (la base des tourelles vient du vanilla, `block-<taille>`) |
| Minerai (`OreBlock`) | `<nom>1`, `<nom>2`, `<nom>3` |
| Convoyeur | `<nom>-<forme 0..6>-<image 0..3>` (28 fichiers) |
| Foreuse | `<nom>`, `<nom>-rotator`, `<nom>-top` |
| Unité terrestre (mécha) | `<nom>`, `<nom>-leg`, `<nom>-base` |
| Unité volante | `<nom>` |
| Arme nommée `minclaude-x` | `x` (les noms d'armes ne sont pas préfixés automatiquement : on écrit le préfixe dans le code) |

Les unités : `constructor = MechUnit::create` (mécha) ou `UnitEntity::create` (volant), sinon le jeu ne sait pas les créer. Le contour des unités est généré par le jeu.

## Conventions

- Style de code de Mindustry (accolades sans espace, `{{ }}` pour configurer le contenu).
- Logique sans dépendance au jeu → paquet `stats` (ou un futur paquet pur), avec tests unitaires.
- Liaison avec le jeu → test d'intégration dans `src/integrationTest` (un JVM par classe, car `Vars` est statique).
- Commentaires et documentation en français. Les clés de traduction et les noms internes restent en anglais.

## Tests d'intégration headless : fonctionnement

`HeadlessGame.start()` reproduit le démarrage des tests officiels de Mindustry. Il lance `HeadlessApplication`, puis `Vars.init()`, le contenu vanilla, le contenu du mod et `Logic`. Il charge ensuite la carte `serpulo/groundZero`, puis arrête la boucle headless pour que le test pilote le jeu lui-même :

- `Time.setDeltaProvider(() -> 1f)` : 1 tick de jeu par appel ;
- `Events.fire(Trigger.update)` : un tick côté tracker ;
- `SaveIO.save(fichier)` / `SaveIO.load(fichier)` : aller-retour de sauvegarde réel.

Le répertoire de travail des tests est le contenu décompressé d'`assets.jar` (`build/game-assets/assets`). La racine du projet est transmise par la propriété système `minclaude.projectDir`.

Limites : en headless il n'y a ni rendu ni `Vars.ui`. L'interface se vérifie donc en jeu, d'où l'importance de garder la logique hors de `ui/`.

## Autotest de l'interface dans le vrai client

Le scénario V1 construit aussi une **base de démonstration** près du noyau, avec chaque bâtiment du mod dans un état différent (sans entrée, sans énergie, hors gisement…) et les 4 unités du mod. Il vérifie le relevé, capture les onglets Énergie, Industries et Défense, la base, les minerais et les fiches du nouveau contenu.

`./gradlew selfTest` lance Mindustry (`/Applications/Mindustry.app`) avec **un dossier de données isolé** (`-Dmindustry.data.dir=build/selftest/data`) contenant seulement MinClaude, et la variable `MINCLAUDE_SELFTEST=build/selftest/out`. La classe `minclaude.debug.SelfTest` déroule alors un scénario :

1. lancer Ground Zero, injecter 15 min d'historique synthétique, mettre en pause ;
2. vérifier le mini-panneau (présent, visible, dans l'écran, rempli) et le capturer ;
3. alerte, dashboard (4 plages, 3 ressources), options (MinClaude et une page vanilla de référence), commandes, fiches du contenu : une capture pour chacun ;
4. écrire `report.txt` (`ok` / `FAIL` par étape) puis quitter. La tâche échoue si le rapport n'est pas « RÉSULTAT : OK ».

Les captures sont prises après le dessin de l'UI (`Trigger.uiDrawEnd`). Il faut les **regarder** : elles montrent ce qu'aucune assertion ne voit (chevauchements, textes coupés, couleurs). Sans la variable d'environnement, `SelfTest` n'est jamais activé.

À chaque nouvel écran, ajouter ses étapes dans `SelfTest.start()`.

## Équilibrage

`BalanceIT` (dans `integrationTest`) compare chaque contenu au vanilla de même rôle et réécrit `docs/equilibrage.md` à chaque passage :

- murs : PV par valeur de coût ;
- tourelles : DPS de la meilleure munition par valeur de coût ;
- usines : valeur produite / consommée, comparée à la médiane vanilla ;
- unités : puissance √(PV effectifs × DPS) par rang et lignées croissantes ;
- simulation de combat : temps de survie face à 3 duos et 2 lancers ;
- générateurs (énergie par case et par coût), batteries (capacité par coût), cuves, réparation (réparation × surface par coût) ;
- progression simulée (`Progression`) : étape de chaque objet et bloc depuis le départ de Ground Zero d'après les vraies recettes ; tout doit être atteignable, aucun bloc ne doit être rangé dans l'arbre sous un parent utilisable plus tard que lui, et chaque étape indique la place des générateurs et batteries nécessaires aux usines du mod.

Le test échoue si une valeur sort des bornes. **Tout nouveau contenu doit y trouver sa place.** Une nouvelle unité doit recevoir un rang dans `BalanceIT.units`, sinon le test échoue.

## Animations

- **Lueur** : `MCBlocks.glow(bloc, couleur)` ajoute `DrawGlowRegion` au dessin du bloc. Le sprite `<nom>-glow.png` (blanc sur transparent) est généré par `SpriteGenerator.glow` pour les noms listés dans `GLOWING`.
- **Chaleur** : toute `ItemTurret` du mod a un sprite `<nom>-heat.png`, que le jeu teinte en rouge après chaque tir.
- **Rotor** : un bloc ajouté à `MCBlocks.rotors` a un sprite `<nom>-rotator.png` qui tourne quand il produit (turbine industrielle).
- **Fenêtres** : les batteries ont un dessus `<nom>-top.png` percé de fenêtres où le jeu affiche la charge ; les cuves (`LiquidRouter`) ont un fond `<nom>-bottom.png` sous le liquide, et un dessus percé. Les projecteurs de réparation ont une lueur `<nom>-top.png`.

`ContentIT` exige ces régions.

## Monter de version de Mindustry

1. Mettre à jour `mindustryVersion` dans `build.gradle.kts` (le tag GitHub, par exemple `v161`).
2. `./gradlew check`. Les tests d'intégration démarrent le nouveau jeu et détectent les changements d'API ou de format.
3. Si besoin, ajuster `minGameVersion` dans `mod.hjson`, puis noter la nouvelle version dans `Etat.md`.
