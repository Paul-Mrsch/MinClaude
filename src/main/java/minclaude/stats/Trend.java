package minclaude.stats;

/** Tendances et prévisions sur des séries régulières. */
public final class Trend{
    private Trend(){}

    /** Pente (unités par seconde) par moindres carrés ; 0 avec moins de deux points. */
    public static float slope(float[] values, float stepSeconds){
        int n = values.length;
        if(n < 2) return 0f;
        double meanX = (n - 1) / 2.0, meanY = 0;
        for(float v : values) meanY += v;
        meanY /= n;
        double num = 0, den = 0;
        for(int i = 0; i < n; i++){
            double dx = i - meanX;
            num += dx * (values[i] - meanY);
            den += dx * dx;
        }
        return (float)(num / den / stepSeconds);
    }

    /** Moyenne des valeurs, 0 si vide. */
    public static float mean(float[] values){
        if(values.length == 0) return 0f;
        double s = 0;
        for(float v : values) s += v;
        return (float)(s / values.length);
    }

    /**
     * Secondes avant que {@code current} atteigne {@code target} en suivant {@code slope} (unités par seconde).
     * {@link Float#POSITIVE_INFINITY} si la cible n'est jamais atteinte, 0 si elle l'est déjà.
     */
    public static float secondsUntil(float current, float slope, float target){
        float gap = target - current;
        if(gap == 0f) return 0f;
        if(slope == 0f || Math.signum(gap) != Math.signum(slope)) return Float.POSITIVE_INFINITY;
        return gap / slope;
    }
}
