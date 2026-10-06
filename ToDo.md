# MinClaude — À faire

_Mis à jour le 2026-10-06._ Chaque tâche cite son artefact (voir [docs/PLAN.md](docs/PLAN.md)).
Une tâche n'est terminée que si ses **tests** et sa **doc** sont faits, et si `./gradlew check selfTest` passe.
Branches : on développe sur `dev`, on fusionne dans `main` à chaque version publiée (tag `v0.x.0` puis `v1.x.y`).

## Maintenant : après la publication de la 1.0

- [ ] **Partie réelle** (à faire par le joueur) : une partie complète sur Serpulo, de Ground Zero au contenu de fin de jeu, en Normal puis en Brutal
- [ ] Noter les retours d'équilibrage ci-dessous : ils alimentent une 1.0.1
- [x] Campagne de tests globale (unitaires, intégration, 6 autotests, autotest avec les autres mods)
- [x] Fusionner `dev` dans `main`, tag `v1.0.0`, pousser
- [ ] (Si souhaité) Créer une release GitHub `v1.0.0` avec `MinClaude.jar` en pièce jointe

## Retours d'équilibrage du joueur

_À compléter après les parties réelles. Chaque retour devient une correction et, si possible, une borne dans `BalanceIT`._

## Après la 1.0 (idées)

- Export CSV de l'historique
- Plusieurs ressources sur le même graphique
- Panneau déplaçable par glisser-déposer
- Onglet Industries : clic sur une usine bloquée pour centrer la caméra dessus
- Objectifs de stock proposés selon la demande installée
- Adaptation : famille « anti-unités » contre les armées alliées nombreuses
- Build Android (`jarAndroid` du template) si le mod doit tourner sur mobile
- Animations supplémentaires : pistons de la presse, rotation du laveur, bobines de la tisseuse
