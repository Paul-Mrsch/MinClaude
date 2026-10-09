# Journal des versions

## 1.1.0 — V5 (2026-10-08)

- **Énergie** : batterie en invar (2x2), condensateur quantique (3x3, fin de jeu), turbine industrielle (3x3, combustible + eau), générateur à saumure (2x2, sans combustible), nœud électrique longue portée (2x2, portée 28).
- **Bâtiments utilitaires** : grande cuve (4x4, 4 000 de liquide), Tempête (tourelle 3x3 à trois tubes, sol et air, entre le Cyclone et le canon électrique), dôme de restauration (réparation de zone, renforcé par le cristal quantique).
- **Équilibrage** : nouvelles bornes pour les générateurs, batteries, cuves et la réparation, et une **progression simulée** de Ground Zero à la fin de jeu à partir des vraies recettes. Elle a montré que le laveur de minerai, utilisable avant le séparateur, était rangé sous lui dans l'arbre : il passe sous le pulvérisateur.

## 1.0.0 — V4 (2026-10-05)

- **Équilibrage** : rapport automatique [docs/equilibrage.md](docs/equilibrage.md), qui compare chaque contenu au jeu de base, et simulation de combat. Brute, Mastodonte, Seigneur de guerre, Sapeur, Électrocuteur et Essaimeur sont renforcés pour tenir leur rang. La fonderie de laiton demande 3 cuivre au lieu de 2.
- **Graphismes** : lueur animée des fours et réacteurs pendant la production, canons qui rougissent après le tir.
- **Interface** : le fond du dashboard est plus opaque, donc lisible au-dessus d'autres fenêtres.
- **Qualité** : mesure des images par seconde dans le vrai client (117 i/s en combat). Compatibilité vérifiée avec Exogenesis et New Horizon.

## 0.4.0 — V3

- **Contenu complet** : 16 ressources, 15 industries, 17 bâtiments, 15 unités alliées, 16 ennemis (dont 2 boss).
- **IA** : adaptation des vagues à la défense du joueur.
- **Gestion** : mesure exacte des entrées, sorties et pertes du noyau.
- **Performance** : mesurée et bornée par des tests.

## 0.3.0 — V2

- **Contenu** : acier, invar, chrome, saumure, azote liquide, lignée T2/T3, 3 ennemis.
- **IA** : tactiques de groupe (regroupement, flanquement, retraite), IA des volants.
- **Difficulté** : elle règle aussi les PV et les dégâts des ennemis.
- **Gestion** : objectifs de stock et capacité installée.
- **Graphismes** : vraies formes de virage et de jonction pour les convoyeurs.

## 0.2.0 — V1

- **Dashboard complet** : Ressources, Énergie, Industries (goulots), Défense.
- **Premier lot de contenu** et minerais générés.
- **IA** : ciblage des points faibles.
- **Graphismes** : refonte dans un style moderne.

## 0.1.0 — V0 (MVP)

- Suivi des ressources dans le temps, mini-panneau, dashboard, alertes, historique sauvegardé.
