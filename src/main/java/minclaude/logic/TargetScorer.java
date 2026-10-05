package minclaude.logic;

/**
 * IA ennemie, ciblage intelligent : note chaque cible possible, la plus haute note l'emporte.
 * Les ennemis visent d'abord les points faibles qui désorganisent une base (énergie, tourelles à sec,
 * logistique) plutôt que le premier mur venu. La distance pénalise, pour éviter les traversées de carte.
 */
public final class TargetScorer{
    public enum Kind{
        CORE(40f),
        TURRET(55f),
        TURRET_NO_AMMO(90f),
        GENERATOR(100f),
        POWER_NODE(70f),
        CONVEYOR(45f),
        CRAFTER(60f),
        UNIT(50f),
        WALL(5f),
        OTHER(20f);

        public final float value;

        Kind(float value){
            this.value = value;
        }
    }

    /** Paramètres liés à la difficulté. */
    public record Profile(boolean enabled, float seekTiles, float distancePenalty, float lowHealthBonus){
        /** Difficulté 1 à 4 (Facile → Brutal). Facile = comportement vanilla. */
        public static Profile forDifficulty(int difficulty, boolean smartAi){
            if(!smartAi || difficulty <= 1) return new Profile(false, 0f, 1f, 0f);
            return switch(difficulty){
                case 2 -> new Profile(true, 10f, 2.5f, 20f);
                case 3 -> new Profile(true, 16f, 1.8f, 30f);
                default -> new Profile(true, 24f, 1.2f, 40f);
            };
        }
    }

    private TargetScorer(){}

    /**
     * @param kind           type de cible
     * @param distanceTiles  distance en cases
     * @param healthFraction PV restants (0..1) : une cible presque détruite est finie en priorité
     * @param inWeaponRange  la cible est déjà à portée de tir (bonus : pas de détour)
     */
    public static float score(Profile p, Kind kind, float distanceTiles, float healthFraction, boolean inWeaponRange){
        float s = kind.value;
        s += (1f - clamp(healthFraction)) * p.lowHealthBonus;
        s -= distanceTiles * p.distancePenalty;
        if(inWeaponRange) s += 15f;
        return s;
    }

    /** Une cible n'est retenue que si elle est dans le rayon de recherche (au-delà, on suit le chemin vers le noyau). */
    public static boolean inSeekRange(Profile p, float distanceTiles){
        return p.enabled && distanceTiles <= p.seekTiles;
    }

    private static float clamp(float v){
        return Math.max(0f, Math.min(1f, v));
    }
}
