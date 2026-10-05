# Équilibrage de MinClaude

_Généré automatiquement par `BalanceIT` (`./gradlew integrationTest`) à partir du vrai jeu : ne pas modifier à la main._

Chaque contenu du mod est comparé au vanilla de même rôle. Le **ratio** compare la valeur du mod à la médiane du vanilla : 1,0 = identique. Les tests échouent hors des bornes indiquées.

## Tourelles : dégâts par seconde (meilleure munition) par valeur de coût

| Tourelle | DPS | Portée | Valeur | DPS / valeur | Ratio |
|---|---|---|---|---|---|
| duo (vanilla) | 54.00 | 20.00 | 17.50 | 3.09 | — |
| hail (vanilla) | 58.75 | 29.38 | 37.00 | 1.59 | — |
| scatter (vanilla) | 345 | 27.50 | 74.00 | 4.66 | — |
| lancer (vanilla) | 630 | 20.63 | 157 | 4.01 | — |
| arc (vanilla) | 103 | 11.25 | 60.00 | 1.71 | — |
| wave (vanilla) | 80.00 | 13.75 | 133 | 0.60 | — |
| salvo (vanilla) | 300 | 23.75 | 180 | 1.67 | — |
| ripple (vanilla) | 425 | 36.25 | 270 | 1.57 | — |
| fuse (vanilla) | 1080 | 11.25 | 448 | 2.41 | — |
| cyclone (vanilla) | 433 | 25.00 | 329 | 1.32 | — |
| **rivet** | 142 | 18.75 | 82.00 | 1.73 | 1.01 |
| **volley** | 147 | 16.25 | 185 | 0.79 | 0.46 |
| **frost** | 90.00 | 15.00 | 179 | 0.50 | 0.29 |
| **railgun** | 531 | 37.50 | 598 | 0.89 | 0.52 |

Médiane vanilla : 1.71 DPS par unité de valeur. La tourelle Givre (contrôle par le gel) est hors comparaison.

## Unités : puissance par rang

Puissance = √(PV effectifs × DPS). Les unités de soutien (soin, construction) ont peu de DPS : leur ratio est indicatif.

| Rang | Médiane vanilla (PV / DPS / puissance) |
|---|---|
| T1 | 200 / 104 / 146 |
| T2 | 550 / 601 / 635 |
| T3 | 900 / 296 / 571 |
| T4 | 9000 / 948 / 3640 |
| T5 | 22000 / 3328 / 11024 |

| Unité | Camp | Rang | PV | Armure | DPS | Puissance | Ratio |
|---|---|---|---|---|---|---|---|
| warden | allié | T1 | 320 | 3.00 | 133 | 216 | 1.47 |
| aid | allié | T1 | 160 | 0.00 | 0.00 | 12.65 | 0.09 |
| scout | allié | T1 | 180 | 1.00 | 80.00 | 122 | 0.83 |
| engineer | allié | T1 | 240 | 2.00 | 42.00 | 103 | 0.71 |
| skiff | allié | T1 | 280 | 2.00 | 33.33 | 99.47 | 0.68 |
| swarmling | ennemi | T1 | 120 | 0.00 | 24.00 | 53.67 | 0.37 |
| marauder | ennemi | T1 | 480 | 4.00 | 107 | 239 | 1.63 |
| wasp | ennemi | T1 | 200 | 0.00 | 28.00 | 74.83 | 0.51 |
| phantom | ennemi | T1 | 260 | 1.00 | 67.50 | 134 | 0.92 |
| sentinel | allié | T2 | 650 | 6.00 | 270 | 455 | 0.72 |
| relay | allié | T2 | 420 | 2.00 | 20.00 | 94.36 | 0.15 |
| corvette | allié | T2 | 650 | 4.00 | 109 | 282 | 0.44 |
| stalker | ennemi | T2 | 600 | 4.00 | 180 | 348 | 0.55 |
| sapper | ennemi | T2 | 520 | 3.00 | 96.00 | 233 | 0.37 |
| shocker | ennemi | T2 | 720 | 5.00 | 96.00 | 282 | 0.44 |
| hornet | ennemi | T2 | 380 | 3.00 | 120 | 223 | 0.35 |
| bastion | allié | T3 | 1400 | 10.00 | 165 | 548 | 0.96 |
| beacon | allié | T3 | 900 | 4.00 | 54.00 | 233 | 0.41 |
| frigate | allié | T3 | 1300 | 7.00 | 79.29 | 353 | 0.62 |
| ravager | ennemi | T3 | 950 | 8.00 | 202 | 487 | 0.85 |
| siegebreaker | ennemi | T3 | 1300 | 7.00 | 169 | 515 | 0.90 |
| gunship | ennemi | T3 | 1100 | 5.00 | 168 | 461 | 0.81 |
| citadel | allié | T4 | 5200 | 12.00 | 467 | 1817 | 0.50 |
| sanctum | allié | T4 | 4500 | 8.00 | 111 | 789 | 0.22 |
| brute | ennemi | T4 | 6000 | 12.00 | 272 | 1491 | 0.41 |
| juggernaut | ennemi | T4 | 7000 | 14.00 | 240 | 1545 | 0.42 |
| dreadwing | ennemi | T4 | 3800 | 9.00 | 338 | 1276 | 0.35 |
| colossus | allié | T5 | 18000 | 16.00 | 1520 | 6363 | 0.58 |
| halo | allié | T5 | 12000 | 12.00 | 300 | 2213 | 0.20 |
| warlord | ennemi | T5 | 22000 | 18.00 | 941 | 5647 | 0.51 |
| leviathan | ennemi | T5 | 12000 | 14.00 | 1005 | 4138 | 0.38 |

## Murs : PV par valeur de coût

Médiane vanilla (cuivre → surtension, 1x1) : 107

| Mur | PV | Valeur du coût | PV / valeur | Ratio |
|---|---|---|---|---|
| cobalt-wall | 520 | 7.20 | 72.22 | 0.68 |
| cobalt-wall-large | 2080 | 28.80 | 72.22 | 0.68 |
| nickel-wall | 380 | 5.40 | 70.37 | 0.66 |
| nickel-wall-large | 1520 | 21.60 | 70.37 | 0.66 |
| steel-wall | 640 | 9.00 | 71.11 | 0.67 |
| steel-wall-large | 2560 | 36.00 | 71.11 | 0.67 |
| cermet-wall | 800 | 12.00 | 66.67 | 0.63 |
| cermet-wall-large | 3200 | 48.00 | 66.67 | 0.63 |

## Simulation de combat : survie face à une défense type

3 duos au graphite + 2 lancers (alimentés), unité immobilisée à 5 cases, IA vanilla. Temps de destruction, plafonné à 60 s.

| Unité | Rang | Survie (s) | Référence vanilla (s) | Ratio |
|---|---|---|---|---|
| swarmling | T1 | 1.53 | dagger : 1.55 | 0.99 |
| marauder | T1 | 2.78 | dagger : 1.55 | 1.80 |
| stalker | T2 | 3.35 | mace : 3.35 | 1.00 |
| sapper | T2 | 2.80 | mace : 3.35 | 0.84 |
| shocker | T2 | 3.35 | mace : 3.35 | 1.00 |
| ravager | T3 | 4.85 | fortress : 4.87 | 1.00 |
| siegebreaker | T3 | 6.02 | fortress : 4.87 | 1.24 |
| warden | T1 | 2.02 | dagger : 1.55 | 1.30 |
| sentinel | T2 | 3.35 | mace : 3.35 | 1.00 |
| bastion | T3 | 7.35 | fortress : 4.87 | 1.51 |

## Usines : valeur produite / valeur consommée (par cycle)

Les liquides et l'énergie ne sont pas valorisés : le vanilla « perd » donc aussi de la valeur. Le ratio du mod est comparé à la médiane du vanilla.

| Usine | Ratio valeur | Énergie / s | Ratio / vanilla |
|---|---|---|---|
| graphite-press (vanilla) | 0.50 | 0.00 | — |
| silicon-smelter (vanilla) | 0.27 | 30.00 | — |
| kiln (vanilla) | 0.88 | 36.00 | — |
| plastanium-compressor (vanilla) | 0.65 | 180 | — |
| phase-weaver (vanilla) | 0.09 | 300 | — |
| surge-smelter (vanilla) | 0.14 | 240 | — |
| pyratite-mixer (vanilla) | 0.23 | 12.00 | — |
| blast-mixer (vanilla) | 0.50 | 24.00 | — |
| pulverizer (vanilla) | 2.00 | 30.00 | — |
| **cobalt-smelter** | 0.50 | 36.00 | 1.00 |
| **aluminum-smelter** | 0.45 | 48.00 | 0.91 |
| **brass-foundry** | 1.00 | 30.00 | 2.00 |
| **steel-furnace** | 0.75 | 60.00 | 1.50 |
| **alloy-press** | 1.03 | 72.00 | 2.06 |
| **duralumin-forge** | 0.75 | 96.00 | 1.50 |
| **cermet-kiln** | 0.65 | 120 | 1.29 |
| **carbon-weaver** | 0.49 | 132 | 0.98 |
| **quantum-resonator** | 0.53 | 360 | 1.05 |

Médiane vanilla : 0.50.


## Valeurs hors bornes

Aucune.
