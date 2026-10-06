# MinClaude

Mod Java pour **Mindustry v160** (PC), version **1.0.0**.

- **Gestion** : suivi des ressources dans le temps (graphiques, flux exacts du noyau, prévisions, objectifs, alertes) et dashboard (énergie, industries et goulots, défense).
- **Contenu Serpulo** : au moins 15 ressources, industries, bâtiments, unités alliées et ennemis, dont 2 boss.
- **IA ennemie** : elle vise vos points faibles, attaque en escouades et adapte ses vagues à vos défenses.

## Installer (joueur)

1. Récupérer `MinClaude.jar` (`./gradlew jar` → `build/libs/MinClaude.jar`).
2. Le copier dans `~/Library/Application Support/Mindustry/mods/` (macOS), ou passer par Mindustry > Mods > Importer.
3. Lancer Mindustry 160.4 ou plus récent. Le dashboard s'ouvre avec la touche `K`.

Voir le [guide du joueur](docs/guides/utilisation.md) et le [journal des versions](CHANGELOG.md).

## Développer

```bash
./gradlew check        # tous les tests (unitaires + jeu headless)
./gradlew play         # tests, déploiement dans ~/Library/Application Support/Mindustry/mods, lancement du jeu
```

| Document | Rôle |
|---|---|
| [CAHIER_DES_CHARGES.md](CAHIER_DES_CHARGES.md) | Besoin, issu de l'interview |
| [Etat.md](Etat.md) | État détaillé du projet : avancement, tests, limites |
| [ToDo.md](ToDo.md) | Prochaines tâches |
| [docs/PLAN.md](docs/PLAN.md) | Plan V0 → V4 découpé en artefacts |
| [docs/architecture/architecture.md](docs/architecture/architecture.md) | Architecture, flux de données, format de sauvegarde |
| [docs/guides/utilisation.md](docs/guides/utilisation.md) | Guide du joueur |
| [docs/guides/developpement.md](docs/guides/developpement.md) | Guide du développeur : build, tests, ajout de contenu |
| [docs/reference/modding-mindustry.md](docs/reference/modding-mindustry.md) | Référence de l'API de modding Mindustry v160.5 |
| [docs/images/](docs/images/) | Captures du jeu et planche des sprites |
| [docs/equilibrage.md](docs/equilibrage.md) | Rapport d'équilibrage, généré automatiquement depuis le jeu |
| [CHANGELOG.md](CHANGELOG.md) | Journal des versions |

![Dashboard, onglet Industries](docs/images/onglet-industries.png)
