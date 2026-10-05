# MinClaude — État du projet

_Mis à jour le 2026-10-05 · version du mod **0.4.0** (V3)_

## Résumé

La **V3 est terminée** sur la branche `dev`. Le contenu atteint l'objectif du cahier des charges, **au moins 15 éléments par catégorie**, et c'est vérifié par un test.

- **IA ennemie** : elle couvre maintenant ses 3 axes. Le ciblage intelligent et les tactiques de groupe s'ajoutent à l'**adaptation des vagues** aux défenses du joueur.
- **Flux du noyau** : ils sont **mesurés exactement**. Entrées, sorties et objets perdus quand le noyau est plein ne sont plus estimés.
- **Performance** : elle est mesurée et négligeable.

Tous les tests passent : 69 unitaires, 33 d'intégration headless, et l'autotest dans le vrai client (rapport OK, captures relues).

Versions publiées sur `main` : `v0.1.0` (V0), `v0.2.0` (V1), `v0.3.0` (V2).

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
| V2 (`v0.3.0`) | Contenu lot 2, tactiques de groupe, difficulté complète, objectifs, capacité installée, formes de convoyeur | ✅ publié |
| V3 (`0.4.0`) | Contenu complet (≥ 15 par catégorie), adaptation de l'IA, mesure exacte des flux, performance | ✅ sur `dev` |
| V4 | Équilibrage, polissage, version 1.0 | ⬜ |

### V3 — détail

| # | Artefact | Vérification |
|---|---|---|
| A3.1 | Ressources : duralumin, cermet, fibre de carbone, cristal quantique, acide sulfurique | `ContentIT` |
| A3.2 | Industries : forge à duralumin, usine d'acide, four à cermet, tisseuse de carbone, résonateur quantique | `ContentIT`, capture `13c-base-demo-v3` |
| A3.3 | Bâtiments : murs en cermet, pont en duralumin, canon électrique | `ContentIT`, fiche `22-fiche-canon-electrique` |
| A3.4 | 10 alliés : éclaireur, ingénieur, lignées T4/T5 au sol et dans les airs, 3 navals | `ContentIT` (reconstructeurs exponentiel et tétratif, usine navale, navires reconnus « navals ») |
| A3.5 | 11 ennemis dont 2 boss | `WorldIT` (effet « boss »), fiche `24-fiche-seigneur` |
| A3.6 | Adaptation des vagues | `V3LogicTest`, `V3IT` (Brutal sans anti-aérien : +3 volants ; anti-aérien ajouté : retour exact à la base), capture `12c-defense-adaptation` |
| A3.7 | Mesure exacte des flux | `V3LogicTest`, `V3IT` (40 entrées et 25 sorties dans la même seconde, 35 objets perdus quand le noyau est plein) |
| A3.8 | Performance | `V3IT` : seuils 0,5 ms par tick et 5 ms par seconde |

## Contenu actuel

| Catégorie | Objectif | Actuel | Éléments |
|---|---|---|---|
| Ressources | ≥ 15 | **16** | cobalt, nickel, zinc, bauxite, aluminium, laiton, chrome, acier, invar, duralumin, cermet, fibre de carbone, cristal quantique + liquides : saumure, azote liquide, acide sulfurique |
| Minerais (génération) | — | 5 | cobalt, nickel, zinc, bauxite, chrome |
| Industries | ≥ 15 | **15** | 3 fonderies (cobalt, aluminium, laiton), laveur, foreuse à percussion, four à acier, presse à alliage, mélangeur de saumure, cryogénisateur, électrolyseur, forge à duralumin, usine d'acide, four à cermet, tisseuse de carbone, résonateur quantique |
| Bâtiments | ≥ 15 | **17** | murs cobalt, nickel, acier et cermet (×2 chacun), convoyeurs renforcé et blindé, pont en duralumin, Riveteuse, Salve, Givre, canon électrique, nœud en aluminium, conteneur en invar |
| Unités alliées | ≥ 15 | **15** | Gardien → Sentinelle → Bastion → Citadelle → Colosse ; Secours → Relais → Balise → Sanctuaire → Halo ; Esquif → Corvette → Frégate ; Éclaireur, Ingénieur |
| Ennemis | ≥ 15 | **16** | Maraudeur, Guêpe, Ravageur, Frelon, Brute, Essaimeur, Sapeur, Brise-siège, Électrocuteur, Mastodonte, Traqueur, Fantôme, Cannonière, Aile-terreur, **Seigneur de guerre** et **Léviathan** (boss) |
| IA ennemie | 3 axes | **3** | ciblage intelligent (sol et air), tactiques de groupe, adaptation des vagues |
| Dashboard | 4 onglets | 4 | + pertes du noyau, mesure exacte, adaptation ennemie |

## Tests

| Suite | Commande | Tests | Résultat |
|---|---|---|---|
| Unitaires (logique pure + fichiers du mod) | `./gradlew test` | 69 | ✅ 69/69 |
| Intégration headless (vrai jeu v160.5) | `./gradlew integrationTest` | 33 | ✅ 33/33 |
| Autotest en jeu (client réel 160.4, rendu, UI) | `./gradlew selfTest` | ~35 captures + vérifications | ✅ OK |

### Mesures de performance (`V3IT`, headless, Mac M3)

| Mesure | Résultat | Seuil du test |
|---|---|---|
| Suivi par tick (160 bâtiments, 150 unités ennemies) | **0,011 ms** | < 0,5 ms |
| Relevé de la base + escouades, une fois par seconde | **0,38 ms** | < 5 ms |
| Historique après 1 h de jeu (132 séries) | 533 Ko en mémoire | < 3 Mo |
| Sauvegarde après 1 h de jeu (compressée par le jeu) | **25 Ko** | < 2 Mo |

Une image à 60 i/s dispose de 16,6 ms : le mod en utilise moins de 0,1 %.

## Recette V3 (2026-10-05)

- La troisième zone de la base de démonstration a d'abord été placée dans l'obscurité hors carte, invisible. La recherche de zone exclut maintenant les tuiles sombres et vides.
- **Adaptation** : la première démo était équilibrée (50 % d'anti-aérien), donc sans adaptation, ce qui est le bon comportement. La démo pose maintenant 18 tourelles anti-sol seulement, et l'autotest vérifie que le bonus « volants » apparaît.
- Un ancien test exigeait plus d'unités en Brutal pour **chaque** groupe : il exclut maintenant les boss, limités à un seul exemplaire quelle que soit la difficulté.
- Une erreur de démarrage due à un conflit de nom avec le vanilla affiche maintenant un message clair (« Nom de contenu déjà utilisé par le jeu de base… »).

## Limitations connues

- **Mesure exacte** : seulement pour les noyaux vanilla. Les noyaux d'autres mods, avec leur propre classe, restent estimés : le dashboard indique « estimation ».
- **Adaptation** : appliquée aux groupes des ennemis du mod. Elle est recalculée à chaque vague à partir des tourelles et des murs du joueur.
- **Navals** : ils ne se déplacent que sur l'eau, et l'usine navale vanilla est nécessaire.
- **Tactiques de groupe** : déplacements en ligne droite. Une unité bloquée abandonne l'ordre quelques secondes.
- **Équilibrage** : les valeurs sont proches du vanilla, et les boss et les T5 n'ont pas encore été joués (V4).
- **Minerais, ennemis et difficulté** : appliqués seulement aux nouvelles parties.

## Fichiers clés

- Cahier des charges : [CAHIER_DES_CHARGES.md](CAHIER_DES_CHARGES.md)
- Plan V0 → V4 : [docs/PLAN.md](docs/PLAN.md)
- À faire : [ToDo.md](ToDo.md)
- Architecture : [docs/architecture/architecture.md](docs/architecture/architecture.md)
- Guide joueur : [docs/guides/utilisation.md](docs/guides/utilisation.md)
- Guide développeur : [docs/guides/developpement.md](docs/guides/developpement.md)
- Référence de modding Mindustry v160.5 : [docs/reference/modding-mindustry.md](docs/reference/modding-mindustry.md)
- Captures et planche de sprites : [docs/images/](docs/images/)
