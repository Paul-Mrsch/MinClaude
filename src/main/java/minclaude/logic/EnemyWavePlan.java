package minclaude.logic;

import java.util.List;

/**
 * Vagues ajoutées par le mod aux règles d'une nouvelle partie, selon la difficulté (1 Facile … 4 Brutal).
 * Plus la difficulté est haute, plus les ennemis du mod arrivent tôt et nombreux.
 */
public final class EnemyWavePlan{
    /**
     * @param unit     nom interne de l'unité, sans préfixe du mod
     * @param begin    première vague (index 0)
     * @param spacing  une vague sur {@code spacing}
     * @param amount   nombre d'unités au départ
     * @param scaling  vagues nécessaires pour ajouter une unité
     * @param max      plafond d'unités par point d'apparition
     */
    public record Group(String unit, int begin, int spacing, int amount, float scaling, int max, boolean boss){
        public Group(String unit, int begin, int spacing, int amount, float scaling, int max){
            this(unit, begin, spacing, amount, scaling, max, false);
        }
    }

    private EnemyWavePlan(){}

    public static List<Group> forDifficulty(int difficulty){
        int d = Math.max(1, Math.min(4, difficulty));
        // Normal (2) = référence ; chaque cran avance ou recule l'arrivée de 4 vagues.
        int shift = (2 - d) * 4;
        float scaling = switch(d){
            case 1 -> 4f;
            case 2 -> 3f;
            case 3 -> 2.2f;
            default -> 1.5f;
        };
        int max = 4 + d * 2;
        return List.of(
            new Group("marauder", 12 + shift, 3, 1, scaling, max),
            new Group("wasp", 16 + shift, 4, 1, scaling, max),
            new Group("ravager", 22 + shift, 4, 1, scaling * 1.5f, max / 2),
            new Group("hornet", 26 + shift, 5, 1, scaling * 1.5f, max / 2),
            // Mini-boss : rare et plafonné bas.
            new Group("brute", 35 + shift, 10, 1, scaling * 4f, Math.max(1, d - 1)),
            // V3
            new Group("swarmling", 8 + shift, 2, 3, scaling / 2f, max * 2),
            new Group("phantom", 14 + shift, 3, 2, scaling, max),
            new Group("stalker", 18 + shift, 3, 1, scaling, max),
            new Group("sapper", 20 + shift, 4, 1, scaling * 1.5f, max / 2),
            new Group("shocker", 28 + shift, 4, 1, scaling * 1.5f, max / 2),
            new Group("siegebreaker", 30 + shift, 5, 1, scaling * 2f, max / 2),
            new Group("gunship", 32 + shift, 5, 1, scaling * 2f, max / 2),
            new Group("juggernaut", 40 + shift, 8, 1, scaling * 3f, Math.max(1, d)),
            new Group("dreadwing", 45 + shift, 8, 1, scaling * 3f, Math.max(1, d)),
            // Boss : un seul à la fois, avec l'effet « boss » du jeu (barre de vie, protection).
            new Group("warlord", 60 + shift, 15, 1, 1000f, 1, true),
            new Group("leviathan", 70 + shift, 15, 1, 1000f, 1, true)
        );
    }
}
