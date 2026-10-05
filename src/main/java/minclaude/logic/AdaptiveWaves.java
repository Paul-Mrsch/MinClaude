package minclaude.logic;

import java.util.*;

/**
 * IA ennemie, adaptation : la composition des vagues suit les défenses du joueur.
 * <ul>
 *     <li>peu de tourelles anti-aériennes → plus de volants ;</li>
 *     <li>beaucoup de murs par tourelle → plus d'unités de siège (artillerie, sapeurs) ;</li>
 *     <li>peu de tourelles anti-sol → plus d'unités rapides en nombre (essaims) ;</li>
 *     <li>défense très fournie → plus d'unités blindées.</li>
 * </ul>
 * Le bonus (unités en plus par groupe) est plafonné selon la difficulté : 0 en Facile, 1, 2 puis 3 en Brutal.
 */
public final class AdaptiveWaves{
    public enum Family{ AIR, SIEGE, ARMOR, SWARM }

    public record Adaptation(EnumMap<Family, Integer> bonus){
        public static final Adaptation NONE = new Adaptation(new EnumMap<>(Family.class));

        public int bonus(Family f){
            return bonus.getOrDefault(f, 0);
        }

        public boolean any(){
            return bonus.values().stream().anyMatch(v -> v > 0);
        }
    }

    /** Défense du joueur vue par l'adaptation. */
    public record Defense(int turrets, int antiAir, int antiGround, int walls){}

    private AdaptiveWaves(){}

    public static int cap(int difficulty){
        return Math.max(0, Math.min(3, difficulty - 1));
    }

    public static Adaptation compute(Defense d, int difficulty){
        int cap = cap(difficulty);
        EnumMap<Family, Integer> bonus = new EnumMap<>(Family.class);
        if(cap == 0 || d.turrets() == 0) return new Adaptation(bonus);
        int half = (cap + 1) / 2;
        float air = d.antiAir() / (float)d.turrets(), ground = d.antiGround() / (float)d.turrets();
        float wallsPerTurret = d.walls() / (float)d.turrets();

        if(air < 0.25f) bonus.put(Family.AIR, cap);
        else if(air < 0.5f) bonus.put(Family.AIR, half);

        if(wallsPerTurret >= 8f) bonus.put(Family.SIEGE, cap);
        else if(wallsPerTurret >= 4f) bonus.put(Family.SIEGE, half);

        if(ground < 0.5f) bonus.put(Family.SWARM, cap);

        if(d.turrets() >= 12) bonus.put(Family.ARMOR, half);
        return new Adaptation(bonus);
    }
}
