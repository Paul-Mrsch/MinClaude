# MinClaude — À faire

_Mis à jour le 2026-10-05._ Chaque tâche cite son artefact (voir [docs/PLAN.md](docs/PLAN.md)).
Une tâche n'est terminée que si ses **tests** et sa **doc** sont faits, et si `./gradlew check selfTest` passe.

## Maintenant : clôturer la V1

- [ ] **Partie réelle** (à faire par le joueur) :
  - [ ] Nouvelle partie sur Ground Zero : minerais du mod visibles, minables (foreuse à percussion pour le cobalt)
  - [ ] Construire la chaîne bauxite → aluminium → convoyeur renforcé, puis zinc + cuivre → laiton
  - [ ] Onglet Industries : couper une entrée, puis vérifier le goulot et l'alerte « Usines arrêtées faute de … »
  - [ ] Onglet Énergie : production, consommation et batteries cohérentes avec le jeu
  - [ ] Vague 13 et suivantes : arrivée des maraudeurs, qui visent les générateurs et convoyeurs plutôt que les murs
  - [ ] Gardien (usine terrestre) et Secours (usine aérienne) : construction, combat, soin
  - [ ] Noter les retours d'équilibrage dans ce fichier
- [ ] Fusionner la branche `v1` dans `main`, tag `v0.2.0`, pousser

## V2 — Expansion (10 éléments par catégorie)

### Contenu
- [ ] **A2.1 Ressources** : chrome, acier, invar, liquides (saumure, azote liquide)
- [ ] **A2.2 Industries** : électrolyseur, cryogénisateur, aciérie, presse à alliage, raffinerie chimique
- [ ] **A2.3 Bâtiments** : convoyeur blindé, routeur filtrant, 2 tourelles, stockage
- [ ] **A2.4 Unités alliées** : arbre d'évolution T1 → T3 (reconstructeurs)
- [ ] **A2.5 Ennemis** : ennemis aériens et blindés supplémentaires

### IA et difficulté
- [ ] **A2.6 Tactiques de groupe** : regroupement avant l'assaut, flanquement, retraite si affaiblis ; logique pure testée + IT avec plusieurs unités
- [ ] **A2.7 Difficulté active partout** : PV, dégâts et vagues selon la difficulté (aujourd'hui elle règle seulement l'IA et l'arrivée des ennemis du mod)
- [ ] IA intelligente pour les volants (ciblage selon `TargetScorer`)

### Gestion
- [ ] **A2.8 Prévisions avancées** : objectifs de stock, projection de la production des usines construites
- [ ] Mesure exacte des entrées dans le noyau (interception des transferts) à la place de l'estimation

### Qualité
- [ ] Convoyeur : dessiner les 7 formes (virages, jonctions) au lieu d'une seule
- [ ] Mesurer le coût par tick du tracker et du scanner sur une grande base (objectif < 0,1 ms par tick)
- [ ] Test d'intégration « partie longue » (1 h simulée) : mémoire et taille de la sauvegarde
- [ ] Étendre `selfTest` à chaque nouvel écran ou contenu

## Idées et questions ouvertes

- Export CSV de l'historique (non retenu au cahier des charges, possible en bonus)
- Afficher plusieurs ressources sur le même graphique pour les comparer
- Panneau déplaçable par glisser-déposer
- Dans l'onglet Industries, cliquer sur une usine bloquée pour centrer la caméra dessus
