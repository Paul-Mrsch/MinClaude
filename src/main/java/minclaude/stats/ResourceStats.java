package minclaude.stats;

/**
 * Résumé instantané d'une ressource du noyau, calculé à partir de l'historique.
 *
 * @param stock       quantité actuelle
 * @param capacity    capacité du noyau (0 si inconnue)
 * @param inPerSec    entrées moyennes par seconde sur la fenêtre
 * @param outPerSec   sorties moyennes par seconde sur la fenêtre
 * @param slopePerSec tendance nette du stock (moindres carrés), par seconde
 * @param secondsToEmpty secondes avant épuisement, +inf si le stock ne baisse pas
 * @param secondsToFull  secondes avant que le noyau soit plein, +inf si le stock ne monte pas
 */
public record ResourceStats(float stock, float capacity, float inPerSec, float outPerSec, float slopePerSec,
                            float secondsToEmpty, float secondsToFull){

    /** Fenêtre d'analyse par défaut, en secondes. */
    public static final int DEFAULT_WINDOW = 60;

    public static String stockKey(String item){
        return "item/" + item + "/stock";
    }

    public static String inKey(String item){
        return "item/" + item + "/in";
    }

    public static String outKey(String item){
        return "item/" + item + "/out";
    }

    /** Objets détruits faute de place dans le noyau (mesure exacte seulement). */
    public static String lostKey(String item){
        return "item/" + item + "/lost";
    }

    public static ResourceStats compute(MetricHistory history, String item, float capacity, int windowSeconds){
        TieredSeries stock = history.get(stockKey(item)), in = history.get(inKey(item)), out = history.get(outKey(item));
        if(stock == null) return new ResourceStats(0, capacity, 0, 0, 0, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);

        float[] recent = stock.recent(windowSeconds);
        float current = stock.latest();
        float slope = Trend.slope(recent, 1f);
        float inRate = in == null ? 0f : Trend.mean(in.recent(windowSeconds));
        float outRate = out == null ? 0f : Trend.mean(out.recent(windowSeconds));
        float toEmpty = Trend.secondsUntil(current, slope, 0f);
        float toFull = capacity > 0 ? Trend.secondsUntil(current, slope, capacity) : Float.POSITIVE_INFINITY;
        return new ResourceStats(current, capacity, inRate, outRate, slope, toEmpty, toFull);
    }

    public float netPerSec(){
        return inPerSec - outPerSec;
    }

    public float fillFraction(){
        return capacity <= 0 ? 0f : Math.min(1f, stock / capacity);
    }
}
