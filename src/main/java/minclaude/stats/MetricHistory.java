package minclaude.stats;

import java.util.*;

/**
 * Ensemble des métriques suivies d'une partie, identifiées par une clé texte stable
 * (ex. {@code item/copper/stock}) pour rester lisibles d'une version du mod à l'autre.
 * Les séries créées en cours de partie sont plus courtes : l'affichage les aligne à droite (sur « maintenant »).
 */
public final class MetricHistory{
    private final LinkedHashMap<String, TieredSeries> series = new LinkedHashMap<>();
    private long elapsedSeconds;

    public synchronized void record(String key, float value){
        series.computeIfAbsent(key, k -> new TieredSeries()).add(value);
    }

    /** À appeler une fois par seconde de jeu, après les {@link #record}. */
    public synchronized void tick(){
        elapsedSeconds++;
    }

    public synchronized long elapsedSeconds(){
        return elapsedSeconds;
    }

    synchronized void setElapsedSeconds(long seconds){
        elapsedSeconds = seconds;
    }

    public synchronized TieredSeries get(String key){
        return series.get(key);
    }

    public synchronized boolean has(String key){
        return series.containsKey(key);
    }

    public synchronized SeriesView view(String key, TimeRange range){
        TieredSeries s = series.get(key);
        return s == null ? SeriesView.EMPTY : s.view(range);
    }

    public synchronized List<String> keys(){
        return new ArrayList<>(series.keySet());
    }

    synchronized void put(String key, TieredSeries s){
        series.put(key, s);
    }

    public synchronized int size(){
        return series.size();
    }

    public synchronized void clear(){
        series.clear();
        elapsedSeconds = 0;
    }
}
