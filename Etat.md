# MinClaude — État du projet

_Mis à jour le 2026-10-05 · version du mod **0.3.0** (V2)_

## Résumé

La **V2 est terminée** sur la branche `dev`.

- **Contenu** : il passe à environ 10 éléments par catégorie, avec l'acier, l'invar, le chrome, deux liquides, la lignée d'unités T1 → T3 et trois nouveaux ennemis, dont un mini-boss.
- **IA ennemie** : elle a maintenant des tactiques de groupe (regroupement, flanquement, retraite), et les volants ont aussi un ciblage intelligent.
- **Difficulté** : elle règle aussi les PV et les dégâts des ennemis.
- **Dashboard** : il affiche les objectifs de stock et la capacité de production installée.
- **Convoyeurs** : ils ont leurs vraies formes de virage et de jonction.

Tous les tests passent : 62 unitaires, 27 d'intégration headless, et l'autotest dans le vrai client (rapport OK, captures relues).

Versions publiées : `v0.1.0` (V0) et `v0.2.0` (V1) sur `main`.

## Versions et environnement

| Élément | Valeur |
|---|---|
| Mindustry ciblé (compilation) | **v160.5**, dernière release stable (2026-09-20) |
| Jeu installé sur ce Mac | 160.4, compatible : `minGameVersion: 160.4` |
| Java | sources et cible 17 ; compilé avec le JDK 24 installé |
| Gradle | 9.8.0 via le wrapper |
| Tests | JUnit 5.13 |
| Plateforme | PC (macOS testé). Pas de build Android, pas de multijoueur |
| Dépôt | [github.com/Paul-Mrsch/MinClaude](https://github.com/Paul-Mrsch/MinClaude) (privé) : `main` = versions publiées, `dev` = développement |

## Avancement

Le plan détaillé est dans [docs/PLAN.md](docs/PLAN.md).

| Version | Contenu | État |
|---|---|---|
| V0 (`v0.1.0`) | MVP : suivi des ressources dans le temps, mini-panneau, dashboard, alertes, sauvegarde | ✅ publié |
| V1 (`v0.2.0`) | Dashboard complet, contenu lot 1, IA ciblage, refonte graphique | ✅ publié |
| V2 (`0.3.0`) | Contenu lot 2, tactiques de groupe, difficulté complète, objectifs, capacité installée, formes de convoyeur | ✅ sur `dev` |
| V3 | Contenu complet (≥ 15 par catégorie), adaptation de l'IA, mesure exacte des flux, performance | ⬜ |
| V4 | Équilibrage, polissage, version 1.0 | ⬜ |

### V2 — détail

| # | Artefact | Vérification |
|---|---|---|
| A2.1 | Ressources : chrome, acier, invar, saumure, azote liquide | `ContentIT`, fiche `21-fiche-azote` |
| A2.2 | Industries : four à acier, presse à alliage, mélangeur de saumure, cryogénisateur, électrolyseur | `ContentIT` (recettes), capture `13b-base-demo-v2` |
| A2.3 | Bâtiments : murs en acier, convoyeur blindé, Salve, Givre, conteneur en invar | `ContentIT`, fiche `19-fiche-salve` |
| A2.4 | Alliés : Sentinelle, Bastion, Relais (reconstructeurs vanilla) | `ContentIT`, fiche `20-fiche-bastion` |
| A2.5 | Ennemis : Ravageur, Frelon, Brute | `WorldIT`, `V2LogicTest` |
| A2.6 | Tactiques de groupe + IA des volants | `V2LogicTest`, `SquadIT` (ordres sur de vraies unités, déplacement vers le ralliement), `ContentIT` |
| A2.7 | Difficulté : PV et dégâts des ennemis | `V2LogicTest`, `WorldIT` (Brutal : PV ×1,7 et dégâts ×1,35 pour les vagues seulement) |
| A2.8 | Objectifs de stock, capacité installée | `V2LogicTest`, `BaseIT`, `TrackingIT` (objectif conservé après sauvegarde et chargement), capture `12b-ressources-objectif` |
| A2.9 | Formes de convoyeur | `ContentIT`, capture `13b-base-demo-v2` |

## Contenu actuel

| Catégorie | Objectif V3 | Actuel | Éléments |
|---|---|---|---|
| Ressources | ≥ 15 | 11 | cobalt, nickel, zinc, bauxite, aluminium, laiton, chrome, acier, invar + liquides saumure, azote liquide |
| Minerais (génération) | — | 5 | cobalt, nickel, zinc, bauxite, chrome |
| Industries | ≥ 15 | 10 | fonderies de cobalt, d'aluminium et de laiton, laveur, foreuse à percussion, four à acier, presse à alliage, mélangeur de saumure, cryogénisateur, électrolyseur |
| Bâtiments | ≥ 15 | 13 | murs cobalt (2), nickel (2) et acier (2), convoyeurs renforcé et blindé, Riveteuse, Salve, Givre, nœud en aluminium, conteneur en invar |
| Unités alliées | ≥ 15 | 5 | Gardien → Sentinelle → Bastion, Secours → Relais |
| Ennemis | ≥ 15 | 5 | Maraudeur, Guêpe, Ravageur, Frelon, Brute |
| IA ennemie | 3 axes | 2 | ciblage intelligent (sol et air), tactiques de groupe |
| Dashboard | 4 onglets | 4 | + objectifs de stock et capacité installée (Ressources) |

## Tests

| Suite | Commande | Tests | Résultat |
|---|---|---|---|
| Unitaires (logique pure + fichiers du mod) | `./gradlew test` | 62 | ✅ 62/62 |
| Intégration headless (vrai jeu v160.5) | `./gradlew integrationTest` | 27 | ✅ 27/27 |
| Autotest en jeu (client réel 160.4, rendu, UI) | `./gradlew selfTest` | ~30 captures + vérifications | ✅ OK |

Ce que la V2 ajoute contre les régressions :

- **logique** : escouades (regroupement borné dans le temps, ailes de chaque côté et en avant, retraite temporaire, escouades distinctes, désactivé en Facile), difficulté, modèle de production (capacité, rendement), objectifs (une alerte, réarmement sous 90 %, aller-retour du format, rejet des données corrompues), plan des vagues V2 ;
- **jeu réel** : ordres d'escouade calculés pour de vraies unités ennemies et suivis par l'IA ; multiplicateurs de difficulté appliqués aux vagues seulement ; capacité installée d'une fonderie ; objectif restauré après sauvegarde ; lignée d'unités dans les reconstructeurs ; IA installée sur les volants armés mais pas sur les mineurs ni les constructeurs ;
- **démarrage du jeu** : un conflit de nom avec le contenu vanilla fait échouer tous les tests d'intégration. C'est arrivé deux fois en V2 (`nitrogen` et `electrolyzer` existent déjà dans Erekir), et c'est corrigé.

## Recette V2 (2026-10-05)

- Sprites V2 relus sur une planche agrandie : la goutte de liquide et le virage du convoyeur ont été retouchés.
- **Conflits de nom avec Erekir** : les noms internes ont été renommés en `liquid-nitrogen` et `brine-electrolyzer`. En jeu, le préfixe `minclaude-` évitait déjà le conflit, mais pas en test.
- **Escouades vides en jeu** : la liste des unités d'une équipe n'est mise à jour qu'à la frame suivante. Le gestionnaire parcourt maintenant toutes les unités du jeu.
- **IA volante installée sur le mono (mineur)** : l'installation est réservée aux unités armées et exclut mineurs et constructeurs.
- **Autotest** : la base de démonstration a une deuxième partie avec tout le contenu V2, des convoyeurs en virage et en jonction, et les 6 nouvelles unités. L'autotest capture aussi l'onglet Ressources avec un objectif et 3 nouvelles fiches.

## Limitations connues

- **Entrées et sorties estimées** : elles sont déduites des variations du stock. La mesure exacte est prévue en A3.7.
- **Tactiques de groupe** : déplacements en ligne droite vers les points de ralliement et de flanc. Une unité bloquée par un mur abandonne l'ordre quelques secondes. Les vagues de moins de 3 à 5 unités (selon la difficulté) attendent jusqu'à 10 à 20 s au point d'apparition avant d'attaquer.
- **Difficulté** : les PV et dégâts sont appliqués au début d'une nouvelle partie et enregistrés avec ses règles. Changer l'option en cours de partie ne modifie que l'IA.
- **Capacité installée** : calculée pour les `GenericCrafter`, séparateurs et foreuses. Le laveur de minerai produit au hasard : sa capacité par ressource est une moyenne.
- **Minerais, ennemis et difficulté** : appliqués seulement aux nouvelles parties.
- **Équilibrage** : les valeurs sont proches du vanilla mais n'ont pas encore été jouées longuement (A4.1).

## Fichiers clés

- Cahier des charges : [CAHIER_DES_CHARGES.md](CAHIER_DES_CHARGES.md)
- Plan V0 → V4 : [docs/PLAN.md](docs/PLAN.md)
- À faire : [ToDo.md](ToDo.md)
- Architecture : [docs/architecture/architecture.md](docs/architecture/architecture.md)
- Guide joueur : [docs/guides/utilisation.md](docs/guides/utilisation.md)
- Guide développeur : [docs/guides/developpement.md](docs/guides/developpement.md)
- Référence de modding Mindustry v160.5 : [docs/reference/modding-mindustry.md](docs/reference/modding-mindustry.md)
- Captures et planche de sprites : [docs/images/](docs/images/)
