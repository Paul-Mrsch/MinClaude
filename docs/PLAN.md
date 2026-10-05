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
| A0.13 | Recette en jeu | Autotest dans le client réel (`selfTest`) + partie jouée par le joueur | `selfTest`, journal du jeu | ✅ auto · 🔄 partie réelle |

---

## V1 — Dashboard complet et premier lot de contenu

| # | Artefact | Contenu | Tests |
|---|---|---|---|
| A1.1 | Onglet Énergie | Production, consommation, stockage des batteries, solde, historique (clés `power/*` dans le même historique) | IT : réseau électrique simulé |
| A1.2 | Onglet Industries | État de chaque usine (active, entrée manquante, sortie pleine, sans énergie), rendement, regroupement par type | IT : usine bloquée détectée |
| A1.3 | Onglet Défense | Unités par type, tourelles (munitions, PV), prochaine vague et sa composition | IT |
| A1.4 | Goulots d'étranglement | Pour une ressource, quelles usines en manquent ou en produisent trop. Alerte « usine bloquée » | unitaires + IT |
| A1.5 | Ressources, lot 1 (5) | Minerais **cobalt, nickel, zinc, bauxite** générés sur les secteurs de Serpulo, et l'**aluminium** | IT : présence dans la génération des cartes |
| A1.6 | Industries, lot 1 (5) | Broyeur de bauxite, fonderie d'aluminium, raffinerie de nickel, laminoir, fonderie de cobalt V2 | IT : recettes |
| A1.7 | Bâtiments, lot 1 (5) | Convoyeur renforcé, mur nickel (petit et grand), tourelle légère, pylône électrique | IT |
| A1.8 | Unités alliées, lot 1 (2) | Une unité terrestre et un drone de ravitaillement | IT : spawn, armes |
| A1.9 | Ennemis, lot 1 (2) | Deux unités ennemies insérées dans les vagues par défaut | IT : vagues |
| A1.10 | IA : ciblage intelligent | Les ennemis visent les points faibles (générateurs, convoyeurs, tourelles sans munitions), selon la difficulté | unitaires (score de cible) + IT |
| A1.11 | Sprites V2 | Générateur enrichi : minerais, convoyeurs animés (`-0-0`…`-6-3`), tourelles, unités | `AssetsTest` |

## V2 — Expansion (10 éléments par catégorie)

| # | Artefact | Contenu |
|---|---|---|
| A2.1 | Ressources, lot 2 | Chrome, laiton, acier, invar, liquides (saumure, azote liquide) |
| A2.2 | Industries, lot 2 | Électrolyseur, cryogénisateur, aciérie, presse à alliage, raffinerie chimique |
| A2.3 | Bâtiments, lot 2 | Convoyeur blindé, routeur filtrant, murs avancés, 2 tourelles, stockage |
| A2.4 | Unités alliées, lot 2 | Arbre d'évolution d'unités (T1 → T3) dans une usine dédiée |
| A2.5 | Ennemis, lot 2 | Ennemis aériens et blindés |
| A2.6 | IA : tactiques de groupe | Formations, flanquement, attaques coordonnées, retraite si affaiblis |
| A2.7 | Difficulté réglable active | Les options « Difficulté » et « IA avancée » modulent PV, dégâts, vagues et IA |
| A2.8 | Prévisions avancées | Objectifs de stock, projection de la production à partir des usines construites |

## V3 — Contenu complet (15 éléments ou plus par catégorie)

| # | Artefact | Contenu |
|---|---|---|
| A3.1 | Ressources, lot 3 | Duralumin, cermet, fibre de carbone, pâte de nanites, cristal quantique… (au moins 15 au total) |
| A3.2 | Industries, lot 3 | Chaînes de fin de jeu (au moins 15 au total) |
| A3.3 | Bâtiments, lot 3 | Logistique avancée (pont, transport de masse), tourelles lourdes (au moins 15 au total) |
| A3.4 | Unités alliées, lot 3 | Au moins 15 au total, dont des unités navales |
| A3.5 | Ennemis, lot 3 | Au moins 15 au total, dont des boss |
| A3.6 | IA : adaptation | La composition des vagues s'adapte aux défenses du joueur (anti-aérien faible → plus d'aérien, etc.) |

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
