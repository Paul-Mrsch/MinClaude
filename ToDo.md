# MinClaude — À faire

_Mis à jour le 2026-10-05._ Chaque tâche cite son artefact (voir [docs/PLAN.md](docs/PLAN.md)).
Une tâche n'est terminée que si ses **tests** et sa **doc** sont faits, et si `./gradlew check` passe.

## Maintenant : clôturer la V0

- [x] **A0.13 Recette automatique** (`./gradlew selfTest`) : mini-panneau, alerte, dashboard (4 plages, 3 ressources), options, commandes, fiches du contenu ; 4 défauts corrigés
- [ ] **A0.13 Partie réelle** (à faire par le joueur, ce que l'autotest ne couvre pas) :
  - [ ] Touche `K` au clavier : le dashboard s'ouvre et se ferme
  - [ ] Dépenser des ressources (construire) : tendance rouge, temps avant épuisement, alerte
  - [ ] Sauvegarder, quitter, recharger depuis le menu : l'historique est conservé
  - [ ] Tech tree : rechercher puis construire la fonderie et les murs en cobalt
  - [ ] Noter les retours (position du panneau, couleurs, tailles) dans ce fichier
- [x] Position du mini-panneau : sous la minimap (corrigé après la recette)
- [ ] Mettre le projet sous git (premier commit V0) si souhaité

## V1 — Dashboard complet et premier lot de contenu

### Dashboard
- [ ] **A1.1 Énergie** : collecter production, consommation et stock par réseau (`PowerGraph`) dans `MetricHistory` (clés `power/*`), onglet avec graphiques, test d'intégration avec générateur + batterie + consommateur
- [ ] **A1.2 Industries** : état de chaque `GenericCrafter` (actif, entrée manquante, sortie pleine, sans énergie), liste groupée par type, test d'intégration avec une usine privée d'entrée
- [ ] **A1.3 Défense** : comptage des unités, tourelles sans munitions ou endommagées, prochaine vague, test d'intégration
- [ ] **A1.4 Goulots d'étranglement** : relier ressource ↔ usines consommatrices et productrices ; alerte « usine bloquée » ; étudier une mesure exacte des entrées dans le noyau
- [ ] Activer les onglets et retirer « Disponible en V1 » des bundles

### Contenu (en parallèle)
- [ ] **A1.5 Ressources** : minerais cobalt, nickel, zinc, bauxite (blocs `OreBlock` + génération sur les secteurs de Serpulo) et aluminium ; l'obtention du cobalt passe alors par le minerai
- [ ] **A1.6 Industries** : broyeur de bauxite, fonderie d'aluminium, raffinerie de nickel, laminoir, revoir la fonderie de cobalt
- [ ] **A1.7 Bâtiments** : convoyeur renforcé, murs en nickel, tourelle légère, pylône électrique
- [ ] **A1.8 Unités alliées** : unité terrestre T1 et drone de ravitaillement, avec leur usine
- [ ] **A1.9 Ennemis** : 2 unités insérées dans les vagues par défaut
- [ ] **A1.10 IA, ciblage intelligent** : score de cible pur et testé (générateurs, convoyeurs, tourelles vides), contrôleur assigné aux ennemis du mod puis aux vanilla selon l'option
- [ ] **A1.11 Sprites V2** : minerais, convoyeur animé, tourelle (base + canon), unités (corps, pattes, armes)

### Qualité
- [ ] Étendre `selfTest` à chaque nouvel écran (onglets Énergie, Défense, Industries) : une capture et des vérifications de mise en page par écran
- [ ] Mesurer le coût par tick du tracker sur une grande base (objectif < 0,1 ms)
- [ ] Ajouter un test d'intégration « partie longue » (1 h simulée) pour vérifier la mémoire et la taille de la sauvegarde

## Idées et questions ouvertes

- Export CSV de l'historique (non retenu au cahier des charges, possible en bonus)
- Afficher plusieurs ressources sur le même graphique pour les comparer
- Panneau déplaçable par glisser-déposer
