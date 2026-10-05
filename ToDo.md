# MinClaude — À faire

_Mis à jour le 2026-10-05._ Chaque tâche cite son artefact (voir [docs/PLAN.md](docs/PLAN.md)).
Une tâche n'est terminée que si ses **tests** et sa **doc** sont faits, et si `./gradlew check selfTest` passe.
Branches : on développe sur `dev`, on fusionne dans `main` à chaque version publiée (tag `v0.x.0`).

## Maintenant : clôturer la V3

- [ ] **Partie réelle** (à faire par le joueur) :
  - [ ] Chaîne de fin de jeu : pyratite + eau → acide → cermet et fibre de carbone → cristal quantique (résonateur, azote liquide)
  - [ ] Canon électrique : portée, transpercement, consommation d'énergie
  - [ ] Lignées T4/T5 (reconstructeurs exponentiel et tétratif) et navals (carte avec de l'eau)
  - [ ] Ingénieur : construction et minage
  - [ ] Onglet Défense : construire une défense sans anti-aérien, puis vérifier l'adaptation « +N volants » à la vague suivante
  - [ ] Onglet Ressources : « mesure exacte », et pertes affichées quand le noyau est plein
  - [ ] Vagues 60+ : boss Seigneur de guerre et Léviathan
  - [ ] Noter les retours d'équilibrage dans ce fichier
- [ ] Fusionner `dev` dans `main`, tag `v0.4.0`, pousser

## V4 — Version 1.0

- [ ] **A4.1 Équilibrage** : tableau des coûts, temps de production, PV et dégâts comparés au vanilla (doc) ; ajuster selon les retours du joueur ; simulation automatique de vagues contre une défense type (test d'intégration) pour détecter les ennemis trop forts ou trop faibles
- [ ] **A4.2 Performance** : mesure dans le client réel (avec rendu) sur une grosse partie ; profil des escouades sur 500+ unités
- [ ] **A4.3 Qualité des sprites** : animations (foreuse, creusets lumineux, région `-heat` des tourelles), variantes des grands blocs, revue complète de la planche
- [ ] **A4.4 Compatibilité** : vérifier la dernière version de Mindustry à cette date, monter `mindustryVersion`, tester avec Exogenesis et New Horizon installés (conflits, performances)
- [ ] **A4.5 Publication** : notes de version, page du mod, captures, `README` joueur, `mod.hjson` final

## Idées et questions ouvertes

- Export CSV de l'historique (non retenu au cahier des charges, possible en bonus)
- Afficher plusieurs ressources sur le même graphique pour les comparer
- Panneau déplaçable par glisser-déposer
- Dans l'onglet Industries, cliquer sur une usine bloquée pour centrer la caméra dessus
- Objectifs de stock : proposer un objectif par défaut selon la demande installée
- Adaptation : ajouter des familles « anti-unités » (contre les armées alliées nombreuses)
