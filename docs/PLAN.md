# MinClaude — Plan de réalisation

Ce plan découpe le [cahier des charges](../CAHIER_DES_CHARGES.md) en **versions** (V0 à V4), et chaque version en **artefacts**.
Un artefact est une unité livrable et vérifiable : du code, ses tests et sa documentation. Il est terminé seulement si les trois sont faits.

Conformément au cahier des charges, **tous les axes avancent en parallèle** à chaque version. La V0 est le MVP centré sur la priorité 1.

Légende : ✅ fait · 🔄 en cours · ⬜ à faire

---

## V0 — MVP : gestion des ressources dans le temps (priorité 1)

**Objectif** : en partie, voir l'évolution de chaque ressource du noyau, ses débits et ses échéances, recevoir des alertes, et retrouver l'historique après un rechargement. Valider aussi toute la chaîne de contenu (code, sprites, traductions, tech tree) sur un mini-lot.

| # | Artefact | Contenu | Tests | Statut |
|---|---|---|---|---|
| A0.1 | Projet & build | Gradle 9.8 (wrapper), Java 17, dépendance Mindustry v160.5, `mod.hjson`, tâches `jar`, `deployLocal`, `play` | `check` lance tout | ✅ |
| A0.2 | Moteur d'historique | `stats/` : séries circulaires, séries compactées (toute la partie), séries multi-résolution 1 s / 10 s / 60 s+ | `SeriesTest` | ✅ |
| A0.3 | Analyse | Tendance (moindres carrés), échéances vide/plein, estimation entrées/sorties, mise en forme | `AnalysisTest` | ✅ |
| A0.4 | Alertes | Épuisement proche, stock bas, noyau plein (ressources perdues), avec délai anti-répétition | `AlertEngineTest` | ✅ |
| A0.5 | Persistance | Format binaire versionné + chunk de sauvegarde tolérant aux erreurs | `HistoryCodecTest`, `TrackingIT` (sauvegarde puis chargement) | ✅ |
| A0.6 | Suivi en jeu | `ResourceTracker` : observation à chaque tick, hors pause, enregistrement chaque seconde | `TrackingIT` | ✅ |
| A0.7 | Mini-panneau HUD | Ressources les plus critiques, débit net, échéance, repliable, bouton vers le dashboard | manuel (UI) | ✅ |
| A0.8 | Dashboard, onglet Ressources | Liste, graphiques du stock et des entrées/sorties, plages 1 min / 10 min / 1 h / partie, chiffres clés | manuel (UI) | ✅ |
| A0.9 | Options & raccourci | Catégorie MinClaude dans les paramètres, raccourci `K` modifiable | manuel | ✅ |
| A0.10 | Contenu pilote | Cobalt, fonderie de cobalt, murs en cobalt (petit et grand), tech tree | `ContentIT`, `AssetsTest` | ✅ |
| A0.11 | Générateur de sprites | `generateSprites` : pixel art déterministe, style vanilla | `AssetsTest` (tailles) | ✅ |
| A0.12 | Documentation | Plan, état, ToDo, architecture, guides joueur et développeur, référence de modding | — | ✅ |
| A0.13 | Recette en jeu | Autotest dans le client réel (`selfTest`) + partie jouée par le joueur | `selfTest`, journal du jeu | ✅ |

---

## V1 — Dashboard complet et premier lot de contenu ✅

| # | Artefact | Contenu livré | Tests | Statut |
|---|---|---|---|---|
| A1.1 | Onglet Énergie | Production, consommation, stockage, capacité, couverture de la demande, historique (clés `power/*`) | `BaseLogicTest`, `BaseIT` | ✅ |
| A1.2 | Onglet Industries | État par type d'usine (actives, sans entrée, sans énergie, sortie pleine), rendement | `IndustryTest`, `BaseIT` | ✅ |
| A1.3 | Onglet Défense | Vague, prochaine vague et sa composition, ennemis en vie, tourelles (sans munitions, endommagées), unités alliées | `BaseLogicTest`, `BaseIT` | ✅ |
| A1.4 | Goulots d'étranglement | Ressources qui bloquent le plus d'usines (onglets Industries et Ressources) ; alertes usines, énergie, munitions | `IndustryTest`, `BaseIT` | ✅ |
| A1.5 | Ressources, lot 1 | Cobalt, nickel, zinc, bauxite (minerais générés dans les nouvelles parties), aluminium, laiton | `ContentIT`, `WorldIT`, `WorldLogicTest` | ✅ |
| A1.6 | Industries, lot 1 | Fonderie d'aluminium, fonderie de laiton, laveur de minerai, foreuse à percussion (+ fonderie de cobalt) | `ContentIT` | ✅ |
| A1.7 | Bâtiments, lot 1 | Murs en nickel (2), convoyeur renforcé, riveteuse, nœud en aluminium (+ murs en cobalt) | `ContentIT` | ✅ |
| A1.8 | Unités alliées, lot 1 | Gardien (mécha, usine terrestre), Secours (drone de soin, usine aérienne) | `ContentIT` | ✅ |
| A1.9 | Ennemis, lot 1 | Maraudeur, Guêpe, ajoutés aux vagues selon la difficulté | `WorldIT`, `WorldLogicTest` | ✅ |
| A1.10 | IA : ciblage intelligent | `TargetScorer` + `SmartGroundAI` sur toutes les unités terrestres ennemies | `WorldLogicTest`, `WorldIT` | ✅ |
| A1.11 | Sprites V2 | Minerais, lingots, convoyeur animé, foreuse, tourelle, nœud, méchas, volants, armes ; planche de relecture | `AssetsTest`, `ContentIT` | ✅ |

## V2 — Expansion ✅

| # | Artefact | Contenu livré | Tests | Statut |
|---|---|---|---|---|
| A2.1 | Ressources, lot 2 | Chrome (minerai + électrolyse), acier, invar ; liquides saumure et azote liquide | `ContentIT` | ✅ |
| A2.2 | Industries, lot 2 | Four à acier, presse à alliage, mélangeur de saumure, cryogénisateur, électrolyseur de saumure | `ContentIT` (recettes) | ✅ |
| A2.3 | Bâtiments, lot 2 | Murs en acier (2, absorbent les lasers), convoyeur blindé, Salve (fusil), Givre (lance-liquide gelant), conteneur en invar | `ContentIT` | ✅ |
| A2.4 | Unités alliées, lot 2 | Lignée Gardien → Sentinelle → Bastion, Secours → Relais, dans les reconstructeurs vanilla | `ContentIT` | ✅ |
| A2.5 | Ennemis, lot 2 | Ravageur (blindé), Frelon (bombardier), Brute (mini-boss) | `WorldIT`, `V2LogicTest` | ✅ |
| A2.6 | IA : tactiques de groupe | Escouades : regroupement, flanquement, retraite des unités abîmées (`SquadPlanner`, `SquadManager`) ; IA intelligente des volants | `V2LogicTest`, `SquadIT`, `ContentIT` | ✅ |
| A2.7 | Difficulté active partout | PV et dégâts de l'équipe des vagues selon la difficulté (`DifficultyProfile`), en plus de l'IA et des vagues | `V2LogicTest`, `WorldIT` | ✅ |
| A2.8 | Prévisions avancées | Objectifs de stock (progression, échéance, alerte, sauvegardés) ; capacité installée et demande installée par ressource (`ProductionModel`) | `V2LogicTest`, `BaseIT`, `TrackingIT` | ✅ |
| A2.9 | Convoyeurs | Les 5 formes vanilla (droit, virage, entrée latérale, jonction, double entrée) pour les deux convoyeurs | `ContentIT` (régions), capture `13b-base-demo-v2` | ✅ |

## V3 — Contenu complet (15 éléments ou plus par catégorie)

| # | Artefact | Contenu |
|---|---|---|
| A3.1 | Ressources, lot 3 | Duralumin, cermet, fibre de carbone, pâte de nanites, cristal quantique… (au moins 15 au total) |
| A3.2 | Industries, lot 3 | Chaînes de fin de jeu (au moins 15 au total) |
| A3.3 | Bâtiments, lot 3 | Logistique avancée (pont, transport de masse), tourelles lourdes (au moins 15 au total) |
| A3.4 | Unités alliées, lot 3 | Au moins 15 au total, dont des unités navales |
| A3.5 | Ennemis, lot 3 | Au moins 15 au total, dont des boss |
| A3.6 | IA : adaptation | La composition des vagues s'adapte aux défenses du joueur (anti-aérien faible → plus d'aérien, etc.) |
| A3.7 | Mesure exacte des flux | Intercepter les transferts vers le noyau pour remplacer l'estimation des entrées/sorties |
| A3.8 | Performance | Coût par tick du tracker, du scanner et des escouades sur une grosse base ; test « partie longue » (1 h simulée) |

## V4 — Version 1.0

| # | Artefact | Contenu |
|---|---|---|
| A4.1 | Équilibrage | Coûts, temps de production, PV et dégâts comparés au vanilla, tableau d'équilibrage dans la doc |
| A4.2 | Performance | Mesure du coût par tick (suivi, IA, UI) sur une grosse base, optimisation |
| A4.3 | Qualité des sprites | Retouche du générateur, ombres, animations |
| A4.4 | Compatibilité | Vérification sur la dernière version de Mindustry à cette date, montée de `mindustryVersion` |
| A4.5 | Publication | Notes de version, page du mod, captures d'écran |

---

## Règles de travail

- **Tests contre les régressions** : chaque artefact ajoute ses tests. `./gradlew check` doit passer avant tout déploiement (`deployLocal` en dépend).
  - `src/test` : logique pure et fichiers du mod (rapide, sans le jeu).
  - `src/integrationTest` : le vrai jeu en headless (contenu chargé, partie simulée, sauvegarde puis chargement).
- **Documentation écrite en même temps que le code** : chaque artefact met à jour [Etat.md](../Etat.md), [ToDo.md](../ToDo.md) et les guides concernés.
- **Format de sauvegarde** : toute modification incrémente `HistoryCodec.VERSION` et garde la lecture des anciennes versions (test `formatV1IsStable`).
- **Noms internes stables** : le nom du mod (`minclaude`) et ceux du contenu ne changent jamais, sinon les sauvegardes et la recherche des joueurs sont perdues.
