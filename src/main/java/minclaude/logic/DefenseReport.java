package minclaude.logic;

import java.util.*;

/** Synthèse de la défense : tourelles, unités alliées et prochaine vague. */
public final class DefenseReport{
    public static final DefenseReport EMPTY = new DefenseReport();

    /** Seuil sous lequel une tourelle est considérée comme endommagée. */
    public static final float DAMAGED_FRACTION = 0.5f;

    private int turrets, turretsNoAmmo, turretsDamaged, antiAir, antiGround, walls;
    private final LinkedHashMap<String, Integer> units = new LinkedHashMap<>();
    private final LinkedHashMap<String, Integer> nextWave = new LinkedHashMap<>();
    private int wave;
    private float secondsToNextWave = Float.POSITIVE_INFINITY;
    private int enemiesAlive;

    public void addTurret(boolean hasAmmo, float healthFraction, boolean targetsAir, boolean targetsGround){
        if(targetsAir) antiAir++;
        if(targetsGround) antiGround++;
        addTurret(hasAmmo, healthFraction);
    }

    public void addWall(){
        walls++;
    }

    /** Défense vue par l'adaptation des vagues. */
    public AdaptiveWaves.Defense profile(){
        return new AdaptiveWaves.Defense(turrets, antiAir, antiGround, walls);
    }

    public void addTurret(boolean hasAmmo, float healthFraction){
        turrets++;
        if(!hasAmmo) turretsNoAmmo++;
        if(healthFraction < DAMAGED_FRACTION) turretsDamaged++;
    }

    public void addUnit(String type){
        units.merge(type, 1, Integer::sum);
    }

    public void setWave(int wave, float secondsToNext, int enemiesAlive){
        this.wave = wave;
        this.secondsToNextWave = secondsToNext;
        this.enemiesAlive = enemiesAlive;
    }

    public void addNextWaveUnits(String type, int count){
        if(count > 0) nextWave.merge(type, count, Integer::sum);
    }

    public int turrets(){
        return turrets;
    }

    public int turretsNoAmmo(){
        return turretsNoAmmo;
    }

    public int turretsDamaged(){
        return turretsDamaged;
    }

    /** Unités par type, les plus nombreuses d'abord. */
    public List<Map.Entry<String, Integer>> units(){
        return sorted(units);
    }

    public int unitTotal(){
        return units.values().stream().mapToInt(Integer::intValue).sum();
    }

    public List<Map.Entry<String, Integer>> nextWave(){
        return sorted(nextWave);
    }

    public int nextWaveTotal(){
        return nextWave.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int wave(){
        return wave;
    }

    public float secondsToNextWave(){
        return secondsToNextWave;
    }

    public int enemiesAlive(){
        return enemiesAlive;
    }

    private static List<Map.Entry<String, Integer>> sorted(Map<String, Integer> map){
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()));
        return list;
    }
}
