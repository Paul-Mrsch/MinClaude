# MinClaude — État du projet

_Mis à jour le 2026-10-05 · version du mod **0.1.0** (V0, MVP)_

## Résumé

Le **MVP (V0)** est en place : suivi des ressources dans le temps (priorité 1), dashboard (onglet Ressources), mini-panneau HUD, alertes, options, persistance dans la sauvegarde, plus un petit lot de contenu pilote qui valide la chaîne contenu → sprites → traductions → tech tree.
**Tous les tests passent**, le mod **se charge sans erreur dans le jeu installé**, et **l'interface a été vérifiée dans le vrai client** par l'autotest en jeu (`./gradlew selfTest`, 12 captures, rapport OK). Cinq défauts d'interface (dont un signalé par le joueur) ont été corrigés (voir plus bas).

## Versions et environnement

| Élément | Valeur |
|---|---|
| Mindustry ciblé (compilation) | **v160.5**, dernière release stable (2026-09-20) |
| Jeu installé sur ce Mac | 160.4, compatible : `minGameVersion: 160.4` |
| Java | sources et cible 17 ; compilé avec le JDK 24 installé |
| Gradle | 9.8.0 via le wrapper |
| Tests | JUnit 5.13 |
| Plateforme | PC (macOS testé). Pas de build Android, pas de multijoueur |

## Avancement par artefact (V0)

Détail et versions suivantes : [docs/PLAN.md](docs/PLAN.md).

| # | Artefact | État | Vérification |
|---|---|---|---|
| A0.1 | Projet & build | ✅ | `./gradlew check jar` |
| A0.2 | Moteur d'historique multi-résolution | ✅ | `SeriesTest` (8 tests) |
| A0.3 | Analyse (tendance, échéances, entrées/sorties) | ✅ | `AnalysisTest` (7) |
| A0.4 | Alertes | ✅ | `AlertEngineTest` (6) |
| A0.5 | Persistance dans la sauvegarde | ✅ | `HistoryCodecTest` (7) + `TrackingIT.historySurvivesSaveAndLoad` |
| A0.6 | Suivi en jeu | ✅ | `TrackingIT` (5) dans une vraie partie headless |
| A0.7 | Mini-panneau HUD | ✅ | `selfTest` : présent, visible, dans l'écran, lignes remplies, capture |
| A0.8 | Dashboard, onglet Ressources | ✅ | `selfTest` : ouverture/fermeture, 4 plages, 3 ressources, captures |
| A0.9 | Options & raccourci `K` | ✅ | `selfTest` : catégorie d'options, raccourci listé dans Commandes, captures |
| A0.10 | Contenu pilote (cobalt, fonderie, 2 murs) | ✅ | `ContentIT` (4), `AssetsTest` (5) |
| A0.11 | Générateur de sprites | ✅ | `AssetsTest.spritesAreMultiplesOfATile` |
| A0.12 | Documentation | ✅ | — |
| A0.13 | Recette en jeu | ✅ automatique · 🔄 partie réelle à jouer | `selfTest` OK ; `last_log.txt` sans erreur |

## Tests

| Suite | Commande | Tests | Résultat |
|---|---|---|---|
| Unitaires (logique pure + fichiers du mod) | `./gradlew test` | 37 | ✅ 37/37 |
| Intégration headless (vrai jeu v160.5) | `./gradlew integrationTest` | 10 | ✅ 10/10 |
| Autotest en jeu (client réel 160.4, rendu, UI) | `./gradlew selfTest` | 30 vérifications + 13 captures | ✅ OK |

Ce qui est protégé contre les régressions :

- calculs : séries, compactage sur 100 h de jeu, tendance, échéances, entrées/sorties, mise en forme ;
- alertes : déclenchement unique, réarmement après délai, seuil bas réservé aux ressources déjà abondantes ;
- sauvegarde : aller-retour, poursuite identique après rechargement, refus des données étrangères, futures ou tronquées, **stabilité du format v1**, chunk corrompu sans plantage ;
- jeu réel : comptage par seconde, pause ignorée, réinitialisation à chaque nouvelle carte, **historique restauré après `SaveIO.save` puis `SaveIO.load`** ;
- contenu : enregistré dans le jeu, présent dans le tech tree, recette, sprite de la bonne taille, traductions FR et EN ;
- fichiers : mêmes clés en FR et EN, aucune traduction vide, `mod.hjson` valide (nom, classe principale, `minGameVersion ≥ 154`), icône.

## Contenu actuel

| Catégorie | Objectif V3 | Actuel | Éléments |
|---|---|---|---|
| Ressources | ≥ 15 | 1 | cobalt |
| Industries | ≥ 15 | 1 | fonderie de cobalt |
| Bâtiments | ≥ 15 | 2 | mur en cobalt, grand mur en cobalt |
| Unités alliées | ≥ 15 | 0 | — |
| Ennemis | ≥ 15 | 0 | — |
| IA ennemie | 3 axes | 0 | options déjà enregistrées (difficulté, IA avancée) |
| Dashboard | 4 onglets | 1 | Ressources (Énergie, Défense, Industries visibles mais désactivés) |

## Recette de l'interface (2026-10-05)

L'autotest (`minclaude.debug.SelfTest`) n'est actif que si la variable d'environnement `MINCLAUDE_SELFTEST` est définie. Il tourne dans un dossier de données isolé (`build/selftest/data`), sans toucher aux sauvegardes, aux options ni aux autres mods du joueur. Il lance Ground Zero, injecte 15 min d'historique synthétique, met la partie en pause, parcourt chaque écran, enregistre des captures (`build/selftest/out/*.png`) et un rapport, puis quitte.

Défauts trouvés au premier passage, tous corrigés et vérifiés par un nouveau passage :

| Défaut | Cause | Correction |
|---|---|---|
| Mini-panneau figé sur « Aucune ressource » pendant la pause | minuterie basée sur le temps de jeu (`Interval`), qui est figé en pause | minuterie en temps réel (`Core.graphics.getDeltaTime()`), idem pour le dashboard |
| Mini-panneau chevauchant les textes d'aide du tutoriel (centre-droit) | position centrée verticalement | placé en haut à droite, sous la minimap |
| Colonnes du mini-panneau coupées à droite, symbole ⌛ absent de la police | largeur 240 trop faible | largeur 300, colonnes recalibrées, symbole retiré |
| Courbe du charbon noire, donc illisible (retour du joueur) | le graphique utilisait la couleur brute de la ressource (#272727) sur fond sombre | `Contrast.readableOn` éclaircit toute couleur sous le contraste 4,5:1 (WCAG), teinte conservée ; `GraphColorIT` vérifie toutes les ressources du jeu ; capture `06b-dashboard-charbon` |
| Nombres coupés dans la liste du dashboard, étiquette du maximum sur la courbe | marges | liste élargie, bandes réservées aux étiquettes dans `LineGraph` |

Vérifié, conforme : textes FR, cases à cocher (même rendu que les options vanilla), catégorie « MinClaude » et touche `K` dans Commandes, fiches de la fonderie, du grand mur et du cobalt (sprites, descriptions, recette, 36 énergie/s).

Pas encore vérifié dans le client réel, mais couvert par les tests headless ou unitaires : appui réel sur `K`, alerte déclenchée par une vraie baisse de stock, sauvegarde puis rechargement depuis le menu, recherche et construction dans le tech tree.

## Limitations connues

- **Entrées et sorties estimées** : elles sont déduites des variations du stock à chaque tick, donc une entrée et une sortie dans le même tick se compensent. Le stock et la tendance sont exacts. Une mesure exacte demanderait d'intercepter les transferts vers le noyau : à étudier en V1 (A1.4).
- **Noyau plein** : le surplus détruit par le noyau n'apparaît pas dans les « entrées », d'où l'alerte « noyau plein » pour le signaler.
- **Sprites** : générés par script, corrects mais simples (amélioration prévue en A1.11 et A4.3).
- **Interface** : pas testable en headless. Elle se vérifie avec `./gradlew selfTest` (client réel) en lisant les captures.
- **Capacité affichée** : `storageCapacity` du noyau principal de l'équipe.
- Les autres mods installés (Exogenesis, New Horizon) produisent leurs propres avertissements dans le journal. Ils sont sans rapport avec MinClaude.

## Fichiers clés

- Cahier des charges : [CAHIER_DES_CHARGES.md](CAHIER_DES_CHARGES.md)
- Plan V0 → V4 : [docs/PLAN.md](docs/PLAN.md)
- À faire : [ToDo.md](ToDo.md)
- Architecture : [docs/architecture/architecture.md](docs/architecture/architecture.md)
- Guide joueur : [docs/guides/utilisation.md](docs/guides/utilisation.md)
- Guide développeur : [docs/guides/developpement.md](docs/guides/developpement.md)
- Référence de modding Mindustry v160.5 : [docs/reference/modding-mindustry.md](docs/reference/modding-mindustry.md)
