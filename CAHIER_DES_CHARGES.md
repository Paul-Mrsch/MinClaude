# MinClaude — Cahier des charges

Mod Mindustry, issu de l'interview du 2026-10-05.

## Cadre technique
- **Nom** : MinClaude
- **Compatibilité** : dernière version stable de Mindustry (à vérifier au début du développement)
- **Langage** : Java (JDK 17, Gradle), produit un `.jar`
- **Plateforme** : PC uniquement (pas de build Android, pas de contrainte multijoueur)
- **Langues** : français + anglais (`bundle.properties`, `bundle_fr.properties`)
- **Sprites** : pixel art généré par script, dans le style vanilla
- **Test** : Mindustry installé sur ce Mac ; le `.jar` est copié dans le dossier mods
- **Réalisation** : Claude s'occupe de tout, de A à Z
- **Approche** : toutes les fonctionnalités avancent en parallèle

## Contenu
- **Univers** : prolongement du vanilla, intégré à la tech tree de **Serpulo**
- **Volume** : gros, 15 éléments ou plus par catégorie
  - Nouvelles ressources (minerais, alliages, liquides)
  - Nouvelles industries (fours, raffineries, assembleurs…)
  - Nouveaux bâtiments : convoyeurs, murs, tourelles, logistique, énergie
  - Nouvelles unités alliées
  - Nouveaux ennemis
- **Difficulté** : réglable dans les paramètres du mod (IA avancée activable ou non, niveau de difficulté)

## IA des ennemis
- **Ciblage intelligent** : vise les points faibles (générateurs, convoyeurs, tourelles sans munitions)
- **Tactiques de groupe** : formations, flanquement, attaques coordonnées, retraite quand affaiblis
- **Adaptation** : la composition des vagues s'adapte aux défenses du joueur

## Interface de gestion des ressources dans le temps (PRIORITÉ 1)
- Graphiques historiques des stocks du noyau, par ressource
- Débits de production et de consommation, solde net
- Prévisions (« épuisé dans X min ») et alertes de seuil, détection des goulots d'étranglement
- Historique **enregistré dans la sauvegarde**, vues 1 min / 10 min / 1 h / toute la partie

## Dashboard de gestion
- Ressources (voir ci-dessus)
- Énergie : production, stockage, consommation, historique
- Unités et défense : effectifs, état des tourelles (munitions, dégâts), vagues à venir
- Industries : usines actives ou bloquées (entrée manquante, sortie pleine), rendement
- **Accès** : raccourci clavier configurable, bouton dans le HUD, mini-overlay permanent qui se déplie en dashboard complet
