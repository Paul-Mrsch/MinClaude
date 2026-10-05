# MinClaude — À faire

_Mis à jour le 2026-10-05._ Chaque tâche cite son artefact (voir [docs/PLAN.md](docs/PLAN.md)).
Une tâche n'est terminée que si ses **tests** et sa **doc** sont faits, et si `./gradlew check selfTest` passe.
Branches : on développe sur `dev`, on fusionne dans `main` à chaque version publiée (tag `v0.x.0`).

## Maintenant : clôturer la V2

- [ ] **Partie réelle** (à faire par le joueur) :
  - [ ] Chaîne ferraille + charbon → acier → invar (presse) → convoyeur blindé et conteneur
  - [ ] Chaîne sable + eau → saumure → chrome (électrolyseur) ; eau + aluminium → azote liquide → tourelle Givre
  - [ ] Gardien → Sentinelle (reconstructeur additif) → Bastion (multiplicatif)
  - [ ] Vagues : escouades qui se regroupent, ailes qui contournent, unités abîmées qui reculent
  - [ ] Difficulté Brutal sur une nouvelle partie : ennemis nettement plus résistants
  - [ ] Onglet Ressources : fixer un objectif, vérifier l'échéance et l'alerte « Objectif atteint »
  - [ ] Noter les retours d'équilibrage dans ce fichier
- [ ] Fusionner `dev` dans `main`, tag `v0.3.0`, pousser

## V3 — Contenu complet (≥ 15 par catégorie)

### Contenu
- [ ] **A3.1 Ressources** : 4 de plus (duralumin, cermet, fibre de carbone, cristal quantique…)
- [ ] **A3.2 Industries** : 5 de plus (chaînes de fin de jeu)
- [ ] **A3.3 Bâtiments** : 2 de plus au minimum (pont/transport de masse, tourelle lourde)
- [ ] **A3.4 Unités alliées** : 10 de plus (T4/T5, navales, soutien)
- [ ] **A3.5 Ennemis** : 10 de plus, dont des boss

### IA
- [ ] **A3.6 Adaptation** : composition des vagues selon la défense du joueur (via `DefenseReport` : peu d'anti-aérien → plus de volants, beaucoup de murs → plus d'artillerie)

### Gestion et qualité
- [ ] **A3.7 Mesure exacte des flux** : intercepter les transferts vers le noyau
- [ ] **A3.8 Performance** : coût par tick du tracker, du scanner et des escouades sur une grosse base (objectif < 0,1 ms par tick) ; test « partie longue » (1 h simulée) : mémoire et taille de la sauvegarde
- [ ] Test automatique anti-conflit de noms avec le contenu vanilla (plus clair que l'échec au démarrage)
- [ ] Étendre `selfTest` à chaque nouvel écran ou contenu

## Idées et questions ouvertes

- Export CSV de l'historique (non retenu au cahier des charges, possible en bonus)
- Afficher plusieurs ressources sur le même graphique pour les comparer
- Panneau déplaçable par glisser-déposer
- Dans l'onglet Industries, cliquer sur une usine bloquée pour centrer la caméra dessus
- Objectifs de stock : proposer un objectif par défaut selon la demande installée
