package minclaude.logic;

import java.util.HashSet;
import java.util.Set;

/**
 * Additionne les réseaux électriques d'une équipe. Chaque réseau n'est compté qu'une fois, même si on le
 * rencontre par chacun de ses bâtiments. Les valeurs d'entrée sont par tick (unités du jeu) ; les sorties sont par seconde.
 */
public final class PowerAggregator{
    private static final float TICKS_PER_SECOND = 60f;

    private final Set<Integer> seen = new HashSet<>();
    private float producedPerTick, neededPerTick, stored, capacity;
    private int networks;

    /** @return vrai si ce réseau n'avait pas encore été compté */
    public boolean add(int graphId, float producedPerTick, float neededPerTick, float stored, float capacity){
        if(!seen.add(graphId)) return false;
        this.producedPerTick += producedPerTick;
        this.neededPerTick += neededPerTick;
        this.stored += stored;
        this.capacity += capacity;
        networks++;
        return true;
    }

    public int networks(){
        return networks;
    }

    public float producedPerSecond(){
        return producedPerTick * TICKS_PER_SECOND;
    }

    public float consumedPerSecond(){
        return neededPerTick * TICKS_PER_SECOND;
    }

    public float stored(){
        return stored;
    }

    public float capacity(){
        return capacity;
    }

    /** Part de la demande couverte (0..1) ; 1 s'il n'y a pas de demande. */
    public float satisfaction(){
        if(neededPerTick <= 0f) return 1f;
        return Math.min(1f, producedPerTick / neededPerTick);
    }

    public float balancePerSecond(){
        return producedPerSecond() - consumedPerSecond();
    }
}
