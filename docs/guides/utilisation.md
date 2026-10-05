# Guide du joueur

## Installation

1. Copier `MinClaude.jar` dans le dossier des mods : sur macOS, `~/Library/Application Support/Mindustry/mods/`. On peut aussi passer par Mindustry > Mods > Importer un mod.
2. Relancer Mindustry. Il faut la version 160.4 ou plus récente.

## Suivi des ressources

### Mini-panneau (toujours visible)

À droite de l'écran, pendant une partie :

- les ressources du noyau, **les plus urgentes en premier** (épuisement le plus proche, puis plus forte baisse) ;
- pour chacune : quantité, **tendance nette par minute** (vert si elle monte, rouge si elle baisse), et, en rouge, le **temps avant épuisement** quand le stock baisse ;
- bouton 📊 : ouvre le dashboard ; bouton ▾ : replie ou déplie le panneau.

Une ressource apparaît dès qu'elle entre une première fois dans le noyau.

### Dashboard (touche `K`)

- **Liste à gauche** : toutes les ressources suivies. Cliquer sur l'une d'elles pour l'afficher.
- **Plage de temps** : 1 min, 10 min, 1 h, toute la partie.
- **Graphique du haut** : évolution du stock.
- **Graphique du bas** : entrées (vert) et sorties (rouge), par minute.
- **Chiffres** : stock / capacité, entrées, sorties, tendance nette, « épuisé dans », « plein dans ».
- Les onglets Énergie, Défense et Industries arrivent en V1.

L'historique est **enregistré dans la sauvegarde** : on le retrouve en rechargeant la partie. Le temps est celui du jeu : rien n'est enregistré pendant la pause.

> Les entrées et sorties sont estimées à partir des variations du stock. Si une ressource entre et sort pendant la même fraction de seconde, ces mouvements se compensent. La tendance nette, elle, est exacte.

### Alertes

Des messages s'affichent en haut de l'écran quand :

- **une ressource va s'épuiser** avant le délai choisi (3 min par défaut) ;
- **une ressource passe sous le seuil bas** (5 % de la capacité par défaut), seulement si elle a déjà été abondante ;
- **le noyau est plein** d'une ressource qui continue d'arriver : ce surplus est perdu.

Une même alerte ne se répète pas tant que la situation dure. Après un retour à la normale, elle ne peut revenir qu'au bout de 2 minutes.

## Options (Paramètres > MinClaude)

| Option | Défaut | Effet |
|---|---|---|
| Afficher le panneau des ressources | oui | Affiche ou masque les lignes du mini-panneau |
| Ressources dans le panneau | 6 | Nombre de lignes |
| Alertes de ressources | oui | Active les alertes |
| Seuil de stock bas | 5 % | En pourcentage de la capacité du noyau |
| Prévenir avant épuisement | 3 min | Délai de l'alerte d'épuisement |
| Difficulté / IA ennemie avancée | Normal / oui | Enregistrées dès maintenant, actives à partir de la V2 |

Le raccourci du dashboard se change dans Paramètres > Commandes > MinClaude.

## Contenu ajouté (V0)

| Contenu | Type | Recette / coût | Déblocage |
|---|---|---|---|
| Cobalt | Ressource | — | sous le plomb |
| Fonderie de cobalt | Industrie 2×2 | 2 plomb + 1 sable + 36 énergie/s → 1 cobalt / s | sous la fonderie de silicium |
| Mur en cobalt | Défense 1×1 | 6 cobalt, 520 PV (titane : 440) | sous le mur en titane |
| Grand mur en cobalt | Défense 2×2 | 24 cobalt, 2080 PV | sous le mur en cobalt |
