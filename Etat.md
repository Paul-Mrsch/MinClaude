# MinClaude — État du projet

_Mis à jour le 2026-10-08 · version du mod **1.1.0** (V5)_

## Résumé

**La version 1.0 est publiée** sur `main` (tag `v1.0.0`). Tout le cahier des charges est couvert :

- **Gestion des ressources dans le temps** (priorité 1) : historique sauvegardé, graphiques, flux exacts du noyau, prévisions, objectifs, alertes.
- **Dashboard** : Ressources, Énergie, Industries avec les goulots, Défense avec l'adaptation ennemie ; mini-panneau permanent, raccourci `K`, bouton dans le HUD.
- **Contenu** : au moins 15 éléments par catégorie (ressources, industries, bâtiments, unités alliées, ennemis).
- **IA ennemie** : ciblage des points faibles, tactiques de groupe, adaptation des vagues ; difficulté réglable.

La **V5 (1.1.0)** prolonge jusqu'à la fin de jeu la progression de l'énergie et des bâtiments utilitaires, qui s'arrêtait au milieu de partie : batterie en invar, condensateur quantique, turbine industrielle, générateur à saumure, nœud longue portée, grande cuve, tourelle Tempête et dôme de restauration. Une progression simulée de Ground Zero à la fin de jeu, calculée sur les vraies recettes, vérifie leur place.

La V4 a apporté l'équilibrage (rapport automatique et simulation), les animations, la mesure de fluidité dans le vrai client et la vérification de compatibilité avec Exogenesis et New Horizon.

Tous les tests passent : 69 unitaires, 49 d'intégration headless, l'autotest en jeu et l'autotest avec Exogenesis et New Horizon (V5, 2026-10-09).

Versions publiées sur `main` : `v0.1.0` à `v0.4.0`, puis `v1.0.0` et `v1.1.0` ([release](https://github.com/Paul-Mrsch/MinClaude/releases/tag/v1.1.0)).

## Versions et environnement

| Élément | Valeur |
|---|---|
| Mindustry ciblé (compilation) | **v160.5**, toujours la dernière release stable (vérifié le 2026-10-05) |
| Jeu installé sur ce Mac | 160.4, compatible : `minGameVersion: 160.4` |
| Java | sources et cible 17 ; compilé avec le JDK 24 installé |
| Gradle | 9.8.0 via le wrapper |
| Tests | JUnit 5.13 |
| Plateforme | PC (macOS testé). Pas de build Android, pas de multijoueur |
| Mods testés ensemble | Exogenesis (aureusstratus), New Horizon (yuria-shikibe) |
| Dépôt | [github.com/Paul-Mrsch/MinClaude](https://github.com/Paul-Mrsch/MinClaude) (public, sujet `mindustry-mod` : visible dans le navigateur de mods du jeu) : `main` = versions publiées, `dev` = développement |

## Avancement

Le plan détaillé est dans [docs/PLAN.md](docs/PLAN.md), et le journal des versions dans [CHANGELOG.md](CHANGELOG.md).

| Version | Contenu | État |
|---|---|---|
| V0 (`v0.1.0`) | MVP : suivi des ressources dans le temps | ✅ publié |
| V1 (`v0.2.0`) | Dashboard complet, contenu lot 1, IA ciblage, refonte graphique | ✅ publié |
| V2 (`v0.3.0`) | Contenu lot 2, tactiques de groupe, difficulté, objectifs, convoyeurs | ✅ publié |
| V3 (`v0.4.0`) | Contenu complet, adaptation de l'IA, flux exacts, performance | ✅ publié |
| V4 (`v1.0.0`) | Équilibrage, animations, performance réelle, compatibilité, publication | ✅ publié |
| V5 (`v1.1.0`) | Énergie et bâtiments utilitaires jusqu'à la fin de jeu, progression simulée | ✅ publié |

### V5 — détail

| # | Artefact | Résultat | Vérification |
|---|---|---|---|
| A5.1 | Stockage d'énergie | Batterie en invar (2x2, 36 000, 9 000 par case), condensateur quantique (3x3, 250 000) | `V5IT`, `BalanceIT` (ratio 0,60 et 1,55) |
| A5.2 | Production d'énergie | Turbine industrielle (1 500/s, meilleur générateur continu par case : 167), générateur à saumure (300/s sans combustible) | `V5IT`, `BalanceIT` (ratio par case 2,02 et 0,91) |
| A5.3 | Réseau et liquides | Nœud longue portée (portée 28), grande cuve (4 000) | `V5IT`, `BalanceIT` |
| A5.4 | Défense et soutien | Tempête (754 DPS, portée 32,5, sol et air), dôme de restauration (3,5 fois la couverture du projecteur de réparation) | `V5IT`, `BalanceIT` (ratio 0,86 et 1,49) |
| A5.5 | Progression simulée | De Ground Zero (étape 0) au cristal quantique (étape 6). Avec la V5, faire tourner une usine de chaque type du mod demande 28 % de place de générateurs en moins, et jusqu'à 80 % de place de batteries en moins en fin de jeu. Laveur de minerai déplacé sous le pulvérisateur | `BalanceIT.progression` |
| A5.6 | Sprites et autotest | Zone de démo V5 dans le vrai client : turbine en marche, batteries chargées, régions présentes dans l'atlas ; 118 i/s | `selfTest` |

### V4 — détail

| # | Artefact | Résultat | Vérification |
|---|---|---|---|
| A4.1 | Équilibrage | Rapport [docs/equilibrage.md](docs/equilibrage.md) généré depuis le jeu. 7 valeurs hors bornes trouvées puis corrigées : Brute, Mastodonte, Seigneur de guerre, Sapeur, Électrocuteur et Essaimeur trop faibles pour leur rang ; fonderie de laiton trop généreuse | `BalanceIT` : murs, tourelles, usines, unités, simulation de combat |
| A4.2 | Performance réelle | Client réel, démo en combat : **117 i/s** (8,6 ms par image, 95e centile 10,7 ms) | `selfTest` (seuil 30 i/s) |
| A4.3 | Animations | Lueur pulsée de 9 fours et réacteurs, chaleur des canons de 3 tourelles | `ContentIT` (régions `-glow` et `-heat` exigées) |
| A4.4 | Compatibilité | Avec Exogenesis et New Horizon : aucune erreur, 18 types de noyau instrumentés, IA installée sur 122 types d'unités, 240 i/s | `selfTestCompat` |
| A4.5 | Publication | 1.0.0, `CHANGELOG.md`, `mod.hjson` (sous-titre, description), README joueur | — |

## Contenu

| Catégorie | Objectif | Actuel |
|---|---|---|
| Ressources | ≥ 15 | **16** (13 objets + 3 liquides) |
| Industries | ≥ 15 | **15** |
| Bâtiments | ≥ 15 | **25** (dont 8 d'énergie et de soutien en V5) |
| Unités alliées | ≥ 15 | **15** |
| Ennemis | ≥ 15 | **16** (dont 2 boss) |
| IA ennemie | 3 axes | **3** |
| Dashboard | 4 onglets | **4** |

Le détail est dans le [guide du joueur](docs/guides/utilisation.md), et les chiffres dans le [rapport d'équilibrage](docs/equilibrage.md).

## Tests

| Suite | Commande | Tests | Résultat |
|---|---|---|---|
| Unitaires (logique pure + fichiers du mod) | `./gradlew test` | 69 | ✅ 69/69 |
| Intégration headless (vrai jeu v160.5) | `./gradlew integrationTest` | 49 | ✅ 49/49 |
| Autotest en jeu (client réel 160.4) | `./gradlew selfTest` | 35 captures, 116 vérifications | ✅ OK (V5, 2026-10-08) |
| Autotest avec les autres mods du joueur | `./gradlew selfTestCompat` | idem | ✅ OK (V5, 2026-10-09, 120 i/s) |

### Performance

| Mesure | Résultat | Seuil du test |
|---|---|---|
| Client réel, démo en combat (rendu compris) | 118 i/s, 8,5 ms par image (V5) | ≥ 30 i/s |
| Suivi par tick (headless, 160 bâtiments, 150 unités) | 0,011 ms | < 0,5 ms |
| Relevé de la base + escouades, une fois par seconde | 0,38 ms | < 5 ms |
| Sauvegarde après 1 h de jeu | 25 Ko | < 2 Mo |

## Recette V5 (2026-10-08)

- **Progression simulée** : elle a trouvé que le laveur de minerai, utilisable dès l'étape 1, était rangé sous le séparateur (étape 2). Il passe sous le pulvérisateur. Elle a aussi montré que la première version de la turbine industrielle (1 200/s) ne gagnait que 10 % de place sur le générateur différentiel : elle produit maintenant 1 500/s.
- **Tests instables** : `BaseIT.defenseCountsTurretsUnitsAndWave` échouait environ une fois sur trente : quand les rochers aléatoires repoussaient la zone libre, le Poignard du test naissait sur le noyau et mourait écrasé. Il naît maintenant dans la zone libre. Les tests `V5IT` n'utilisent pas la règle de triche (munitions infinies, bonus) : la Tempête doit d'abord rester muette sans munitions, et le dôme est alimenté par une vraie batterie.

- **Autotest de compatibilité** : il chargeait aussi la 1.0.0 installée depuis le navigateur de mods (`Paul-MrschMinClaude1.zip`), que le jeu gardait à la place de la version testée. Toute autre copie de MinClaude est maintenant exclue. Relancé : 116 vérifications OK avec Exogenesis et New Horizon, aucune erreur dans le journal du jeu.

## Recette V4 (2026-10-05)

- **Équilibrage** : le premier rapport comparait les usines à une règle absolue (« ne pas perdre de valeur »), qui signalait 8 usines. Le vanilla perd aussi de la valeur selon cette mesure (médiane 0,50), car l'énergie et les liquides ne sont pas valorisés. La règle compare donc maintenant le mod au vanilla : seule la fonderie de laiton reste trop généreuse, et elle est corrigée.
- **Compatibilité** : une fenêtre d'actualités de New Horizon restée ouverte se voyait à travers le dashboard. Le fond du dashboard est maintenant plus opaque.
- La planche des sprites exclut les calques blancs de lueur et de chaleur, illisibles sur la planche.

## Campagne de tests globale avant publication (2026-10-06)

- **Problème trouvé** : l'autotest échouait environ une fois sur trois (« zone libre pour la démo V3 », puis « adaptation des vagues » qui en dépend). Les rochers décoratifs, placés au hasard à chaque chargement de Ground Zero, occupaient parfois toutes les zones candidates. Le commit `5cb3c2a` avait été poussé sur `dev` alors que l'autotest échouait ainsi.
- **Correction** : la zone de démo V3 est cherchée sur toute la carte, au plus près du noyau, sans chevaucher la première démo ; les rochers remplaçables comptent comme libres (pas les murs de roche).
- **Résultat** : `clean check` sans cache (69 + 38 tests), 6 passes de `selfTest` sur 6, `selfTestCompat` avec Exogenesis et New Horizon (118 i/s), jar vérifié (mod.hjson, icône, traductions, 221 images).

## Limitations connues

- **Équilibrage** : les bornes automatiques et la progression simulée détectent les erreurs grossières et les trous de progression, mais pas le ressenti (rythme, difficulté) : l'équilibrage fin demande toujours une partie réelle (retours du joueur dans ToDo). La simulation compte les transformations, pas le temps ni les quantités.
- **Mesure exacte** : seulement pour les noyaux de classe vanilla, ce qui inclut ceux d'Exogenesis et de New Horizon qui utilisent cette classe.
- **Tactiques de groupe** : déplacements en ligne droite ; une unité bloquée abandonne l'ordre quelques secondes.
- **Minerais, ennemis et difficulté** : appliqués seulement aux nouvelles parties.
- **Autotest** : le jeu écrit son journal (`last_log.txt`) dans le dossier de données habituel, même avec un dossier de données isolé.

## Fichiers clés

- Cahier des charges : [CAHIER_DES_CHARGES.md](CAHIER_DES_CHARGES.md)
- Plan V0 → V5 : [docs/PLAN.md](docs/PLAN.md)
- Journal des versions : [CHANGELOG.md](CHANGELOG.md)
- À faire : [ToDo.md](ToDo.md)
- Architecture : [docs/architecture/architecture.md](docs/architecture/architecture.md)
- Guide joueur : [docs/guides/utilisation.md](docs/guides/utilisation.md)
- Guide développeur : [docs/guides/developpement.md](docs/guides/developpement.md)
- Équilibrage : [docs/equilibrage.md](docs/equilibrage.md)
- Référence de modding Mindustry v160.5 : [docs/reference/modding-mindustry.md](docs/reference/modding-mindustry.md)
- Captures et planche de sprites : [docs/images/](docs/images/)
