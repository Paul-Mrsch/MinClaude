# MinClaude — État du projet

_Mis à jour le 2026-10-06 · version du mod **1.0.0** (V4)_

## Résumé

**La version 1.0 est publiée** sur `main` (tag `v1.0.0`). Tout le cahier des charges est couvert :

- **Gestion des ressources dans le temps** (priorité 1) : historique sauvegardé, graphiques, flux exacts du noyau, prévisions, objectifs, alertes.
- **Dashboard** : Ressources, Énergie, Industries avec les goulots, Défense avec l'adaptation ennemie ; mini-panneau permanent, raccourci `K`, bouton dans le HUD.
- **Contenu** : au moins 15 éléments par catégorie (ressources, industries, bâtiments, unités alliées, ennemis).
- **IA ennemie** : ciblage des points faibles, tactiques de groupe, adaptation des vagues ; difficulté réglable.

La V4 a apporté l'équilibrage (rapport automatique et simulation), les animations, la mesure de fluidité dans le vrai client et la vérification de compatibilité avec Exogenesis et New Horizon.

Tous les tests passent : 69 unitaires, 38 d'intégration headless, l'autotest en jeu, et l'autotest avec les autres mods.

Versions publiées sur `main` : `v0.1.0` à `v0.4.0`, puis `v1.0.0`.

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
| Dépôt | [github.com/Paul-Mrsch/MinClaude](https://github.com/Paul-Mrsch/MinClaude) (privé) : `main` = versions publiées, `dev` = développement |

## Avancement

Le plan détaillé est dans [docs/PLAN.md](docs/PLAN.md), et le journal des versions dans [CHANGELOG.md](CHANGELOG.md).

| Version | Contenu | État |
|---|---|---|
| V0 (`v0.1.0`) | MVP : suivi des ressources dans le temps | ✅ publié |
| V1 (`v0.2.0`) | Dashboard complet, contenu lot 1, IA ciblage, refonte graphique | ✅ publié |
| V2 (`v0.3.0`) | Contenu lot 2, tactiques de groupe, difficulté, objectifs, convoyeurs | ✅ publié |
| V3 (`v0.4.0`) | Contenu complet, adaptation de l'IA, flux exacts, performance | ✅ publié |
| V4 (`v1.0.0`) | Équilibrage, animations, performance réelle, compatibilité, publication | ✅ publié |

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
| Bâtiments | ≥ 15 | **17** |
| Unités alliées | ≥ 15 | **15** |
| Ennemis | ≥ 15 | **16** (dont 2 boss) |
| IA ennemie | 3 axes | **3** |
| Dashboard | 4 onglets | **4** |

Le détail est dans le [guide du joueur](docs/guides/utilisation.md), et les chiffres dans le [rapport d'équilibrage](docs/equilibrage.md).

## Tests

| Suite | Commande | Tests | Résultat |
|---|---|---|---|
| Unitaires (logique pure + fichiers du mod) | `./gradlew test` | 69 | ✅ 69/69 |
| Intégration headless (vrai jeu v160.5) | `./gradlew integrationTest` | 38 | ✅ 38/38 |
| Autotest en jeu (client réel 160.4) | `./gradlew selfTest` | ~40 captures, 105 vérifications | ✅ OK (6 passes sur 6) |
| Autotest avec les autres mods du joueur | `./gradlew selfTestCompat` | idem | ✅ OK |

### Performance

| Mesure | Résultat | Seuil du test |
|---|---|---|
| Client réel, démo en combat (rendu compris) | 117 i/s, 8,6 ms par image | ≥ 30 i/s |
| Suivi par tick (headless, 160 bâtiments, 150 unités) | 0,011 ms | < 0,5 ms |
| Relevé de la base + escouades, une fois par seconde | 0,38 ms | < 5 ms |
| Sauvegarde après 1 h de jeu | 25 Ko | < 2 Mo |

## Recette V4 (2026-10-05)

- **Équilibrage** : le premier rapport comparait les usines à une règle absolue (« ne pas perdre de valeur »), qui signalait 8 usines. Le vanilla perd aussi de la valeur selon cette mesure (médiane 0,50), car l'énergie et les liquides ne sont pas valorisés. La règle compare donc maintenant le mod au vanilla : seule la fonderie de laiton reste trop généreuse, et elle est corrigée.
- **Compatibilité** : une fenêtre d'actualités de New Horizon restée ouverte se voyait à travers le dashboard. Le fond du dashboard est maintenant plus opaque.
- La planche des sprites exclut les calques blancs de lueur et de chaleur, illisibles sur la planche.

## Campagne de tests globale avant publication (2026-10-06)

- **Problème trouvé** : l'autotest échouait environ une fois sur trois (« zone libre pour la démo V3 », puis « adaptation des vagues » qui en dépend). Les rochers décoratifs, placés au hasard à chaque chargement de Ground Zero, occupaient parfois toutes les zones candidates. Le commit `5cb3c2a` avait été poussé sur `dev` alors que l'autotest échouait ainsi.
- **Correction** : la zone de démo V3 est cherchée sur toute la carte, au plus près du noyau, sans chevaucher la première démo ; les rochers remplaçables comptent comme libres (pas les murs de roche).
- **Résultat** : `clean check` sans cache (69 + 38 tests), 6 passes de `selfTest` sur 6, `selfTestCompat` avec Exogenesis et New Horizon (118 i/s), jar vérifié (mod.hjson, icône, traductions, 221 images).

## Limitations connues

- **Équilibrage** : les bornes automatiques détectent les erreurs grossières, mais l'équilibrage fin demande des parties réelles (retours du joueur dans ToDo).
- **Mesure exacte** : seulement pour les noyaux de classe vanilla, ce qui inclut ceux d'Exogenesis et de New Horizon qui utilisent cette classe.
- **Tactiques de groupe** : déplacements en ligne droite ; une unité bloquée abandonne l'ordre quelques secondes.
- **Minerais, ennemis et difficulté** : appliqués seulement aux nouvelles parties.
- **Autotest** : le jeu écrit son journal (`last_log.txt`) dans le dossier de données habituel, même avec un dossier de données isolé.

## Fichiers clés

- Cahier des charges : [CAHIER_DES_CHARGES.md](CAHIER_DES_CHARGES.md)
- Plan V0 → V4 : [docs/PLAN.md](docs/PLAN.md)
- Journal des versions : [CHANGELOG.md](CHANGELOG.md)
- À faire : [ToDo.md](ToDo.md)
- Architecture : [docs/architecture/architecture.md](docs/architecture/architecture.md)
- Guide joueur : [docs/guides/utilisation.md](docs/guides/utilisation.md)
- Guide développeur : [docs/guides/developpement.md](docs/guides/developpement.md)
- Équilibrage : [docs/equilibrage.md](docs/equilibrage.md)
- Référence de modding Mindustry v160.5 : [docs/reference/modding-mindustry.md](docs/reference/modding-mindustry.md)
- Captures et planche de sprites : [docs/images/](docs/images/)
