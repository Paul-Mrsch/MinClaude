# MinClaude — État du projet

_Mis à jour le 2026-10-05 · version du mod **0.2.0** (V1)_

## Résumé

La **V1 est terminée** sur la branche `v1`.

- Le dashboard a ses 4 onglets : Ressources, Énergie, Industries (avec les goulots d'étranglement), Défense.
- Un premier lot de contenu s'intègre à Serpulo : 6 ressources dont 4 minerais générés dans les nouvelles parties, 5 industries, 7 bâtiments, 2 unités alliées et 2 ennemis ajoutés aux vagues.
- L'IA ennemie a un ciblage intelligent, réglé par la difficulté.

Tous les tests passent : 52 unitaires, 21 d'intégration headless (2 relances sans échec instable) et l'autotest dans le vrai client (23 captures relues, rapport OK).

La V0 (`v0.1.0`) est sur `main`, poussée sur GitHub.

## Versions et environnement

| Élément | Valeur |
|---|---|
| Mindustry ciblé (compilation) | **v160.5**, dernière release stable (2026-09-20) |
| Jeu installé sur ce Mac | 160.4, compatible : `minGameVersion: 160.4` |
| Java | sources et cible 17 ; compilé avec le JDK 24 installé |
| Gradle | 9.8.0 via le wrapper |
| Tests | JUnit 5.13 |
| Plateforme | PC (macOS testé). Pas de build Android, pas de multijoueur |
| Dépôt | [github.com/Paul-Mrsch/MinClaude](https://github.com/Paul-Mrsch/MinClaude) (privé) |

## Avancement

Le plan détaillé est dans [docs/PLAN.md](docs/PLAN.md).

### V0 — MVP (terminé, tag `v0.1.0`)

Suivi des ressources dans le temps, mini-panneau, dashboard (Ressources), alertes, persistance dans la sauvegarde, options, raccourci `K`, contenu pilote, générateur de sprites, autotest en jeu.

### V1 — Dashboard complet et premier lot de contenu (terminé)

| # | Artefact | État | Vérification |
|---|---|---|---|
| A1.1 | Onglet Énergie | ✅ | `BaseLogicTest`, `BaseIT` (panneau solaire), capture `12-onglet-power` |
| A1.2 | Onglet Industries | ✅ | `IndustryTest`, `BaseIT` (usine sans entrée, puis approvisionnée ; usine sans énergie), capture `12-onglet-industry` |
| A1.3 | Onglet Défense | ✅ | `BaseLogicTest`, `BaseIT` (duo sans munitions, dagger, vague), capture `12-onglet-defense` |
| A1.4 | Goulots d'étranglement + alertes | ✅ | `IndustryTest`, `BaseIT` ; alertes « usines arrêtées », « manque d'énergie », « tourelles sans munitions » |
| A1.5 | Ressources, lot 1 | ✅ | `ContentIT`, `WorldIT` (gisements générés une seule fois, de façon déterministe, désactivables) |
| A1.6 | Industries, lot 1 | ✅ | `ContentIT` (recettes) |
| A1.7 | Bâtiments, lot 1 | ✅ | `ContentIT`, capture `13-base-demo` |
| A1.8 | Unités alliées, lot 1 | ✅ | `ContentIT` (usines vanilla), capture `16-fiche-gardien` |
| A1.9 | Ennemis, lot 1 | ✅ | `WorldIT` (ajout aux vagues, désactivable), capture `17-fiche-maraudeur` |
| A1.10 | IA : ciblage intelligent | ✅ | `WorldLogicTest` (score), `WorldIT` (générateur préféré au mur ; Facile = vanilla) |
| A1.11 | Sprites V2 | ✅ | `AssetsTest`, `ContentIT` (toutes les régions requises), planche [docs/images/sprites.png](docs/images/sprites.png) |

## Contenu actuel

| Catégorie | Objectif V3 | Actuel | Éléments |
|---|---|---|---|
| Ressources | ≥ 15 | 6 | cobalt, nickel, zinc, bauxite, aluminium, laiton |
| Minerais (génération) | — | 4 | cobalt, nickel, zinc, bauxite |
| Industries | ≥ 15 | 5 | fonderie de cobalt, fonderie d'aluminium, fonderie de laiton, laveur de minerai, foreuse à percussion |
| Bâtiments | ≥ 15 | 7 | murs cobalt (2), murs nickel (2), convoyeur renforcé, riveteuse (tourelle), nœud en aluminium |
| Unités alliées | ≥ 15 | 2 | gardien (mécha), secours (drone de soin) |
| Ennemis | ≥ 15 | 2 | maraudeur (mécha blindé), guêpe (volant, vise les générateurs) |
| IA ennemie | 3 axes | 1 | ciblage intelligent (unités terrestres) |
| Dashboard | 4 onglets | 4 | Ressources, Énergie, Industries, Défense |

## Tests

| Suite | Commande | Tests | Résultat |
|---|---|---|---|
| Unitaires (logique pure + fichiers du mod) | `./gradlew test` | 52 | ✅ 52/52 |
| Intégration headless (vrai jeu v160.5) | `./gradlew integrationTest` | 21 | ✅ 21/21, stable sur 3 passages |
| Autotest en jeu (client réel 160.4, rendu, UI) | `./gradlew selfTest` | 23 captures + vérifications | ✅ OK |

Ce qui est protégé contre les régressions, en plus de la V0 :

- **logique** : classement des usines (sans entrée, sans énergie, sortie pleine, désactivée), agrégation par type, goulots ; énergie comptée une fois par réseau ; défense ; score de cible de l'IA ; répartition des minerais (déterministe, couverture raisonnable, éligibilité) ; plan des vagues selon la difficulté ; alertes génériques ;
- **jeu réel** : usine privée d'entrée signalée puis active une fois approvisionnée ; usine sans énergie ; énergie historisée ; tourelle sans munitions ; unités ; vague ; gisements ajoutés une seule fois, jamais sous un bloc ni sur un liquide, identiques sur la même carte, désactivables ; ennemis ajoutés aux vagues, désactivables ; IA : générateur préféré au mur le plus proche, comportement vanilla en Facile ;
- **contenu** : enregistré, tech tree (sauf minerais et ennemis), alliés dans les usines vanilla et ennemis dans aucune, IA installée sur les unités terrestres seulement, **toutes les régions de sprites requises par le jeu** (variantes de minerai, 28 images du convoyeur, rotor de foreuse, jambes de mécha, armes), traductions FR et EN avec descriptions ;
- **interface (autotest)** : 4 onglets affichés, base de démonstration relevée (usines, goulot bauxite, énergie, tourelle à sec, unités du mod, foreuse hors gisement), minerais présents sur la carte.

## Recette de l'interface V1 (2026-10-05)

L'autotest construit une base de démonstration près du noyau : chaque bâtiment du mod dans un état différent, avec les 4 unités du mod. Il capture ensuite les 3 nouveaux onglets, la base, les minerais et les fiches. Défauts trouvés et corrigés :

| Défaut | Correction |
|---|---|
| Une courbe plus courte que la plage s'arrêtait aux 3/4 de la largeur | Calcul de position corrigé dans `LineGraph` : le dernier point tombe sur le bord droit |
| Foreuse posée hors gisement classée « Sortie pleine » | Foreuse sans minerai sous elle = « Sans entrée » (vérifié par l'autotest) |
| Textes des goulots coupés, pluriels maladroits (« 1 usines ») | Retour à la ligne ; formulations « Bloque 1 usine(s) », « Usines : 4, dont arrêtées : 4 » |
| Colonne des débits du mini-panneau qui débordait (« -53.7/min ») | Débits arrondis à l'unité dès 10/min (test unitaire), colonne élargie |
| Onglet Défense centré verticalement, unités apparues pendant la pause non comptées | Alignement en haut ; scénario corrigé (unités créées avant le relevé) |

## Refonte graphique (2026-10-05)

À la demande du joueur, tous les sprites ont été redessinés dans un style moderne, inspiré d'Exogenesis sans reprendre ses images :

- aplats facettés avec rampes de 6 tons à décalage de teinte, angles coupés à 45°, biseaux ;
- métal gris-bleu, bandes de couleur vive, motifs symétriques ;
- cristaux et pépites à facettes, lingots en perspective.

Le résultat a été vérifié sur la planche des sprites et dans le jeu (capture `13-base-demo`). Tous les tests passent.

## Limitations connues

- **Entrées et sorties estimées** : elles sont déduites des variations du stock, donc une entrée et une sortie dans le même tick se compensent. Le stock et la tendance sont exacts.
- **IA intelligente** : seulement pour les unités terrestres. Les volants gardent l'IA vanilla ; la guêpe vise déjà les générateurs grâce à `targetFlags`. Les tactiques de groupe et l'adaptation sont prévues en V2 et V3.
- **Minerais** : ajoutés seulement aux nouvelles parties, pas aux sauvegardes existantes. Ils ne remplacent jamais un minerai vanilla et ne se posent ni sous un bloc ni sur un liquide.
- **Ennemis** : ajoutés aux vagues des nouvelles parties qui ont des vagues. Ils arrivent à partir de la vague 13 (maraudeur) et 17 (guêpe) en Normal (index 12 et 16 des règles de vague), et 4 vagues plus tôt par cran de difficulté.
- **Sprites** : générés par script. Les 7 formes du convoyeur utilisent le même dessin droit, donc les virages paraissent droits (tâche dans ToDo).
- **Équilibrage** : les valeurs sont proches du vanilla mais n'ont pas encore été jouées longuement (A4.1).
- Les autres mods installés (Exogenesis, New Horizon) produisent leurs propres avertissements dans le journal. Ils sont sans rapport avec MinClaude.

## Fichiers clés

- Cahier des charges : [CAHIER_DES_CHARGES.md](CAHIER_DES_CHARGES.md)
- Plan V0 → V4 : [docs/PLAN.md](docs/PLAN.md)
- À faire : [ToDo.md](ToDo.md)
- Architecture : [docs/architecture/architecture.md](docs/architecture/architecture.md)
- Guide joueur : [docs/guides/utilisation.md](docs/guides/utilisation.md)
- Guide développeur : [docs/guides/developpement.md](docs/guides/developpement.md)
- Référence de modding Mindustry v160.5 : [docs/reference/modding-mindustry.md](docs/reference/modding-mindustry.md)
- Captures et planche de sprites : [docs/images/](docs/images/)
