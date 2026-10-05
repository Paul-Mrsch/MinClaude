package minclaude.stats;

import java.util.Locale;

/** Mise en forme des nombres et des durées pour l'interface (sans dépendance au jeu, donc testable). */
public final class Format{
    private Format(){}

    /** 1234 -> "1.2k", 2500000 -> "2.5M". */
    public static String amount(float v){
        float a = Math.abs(v);
        String sign = v < 0 ? "-" : "";
        if(a >= 1_000_000f) return sign + trim(a / 1_000_000f) + "M";
        if(a >= 1_000f) return sign + trim(a / 1_000f) + "k";
        return sign + trim(a);
    }

    /** Débit par minute avec signe explicite : "+120/min". Arrondi à l'unité dès 10/min, pour rester court. */
    public static String ratePerMinute(float perSecond){
        float m = perSecond * 60f;
        if(Math.abs(m) >= 10f && Math.abs(m) < 1000f) m = Math.round(m);
        return (m > 0 ? "+" : "") + amount(m) + "/min";
    }

    /** Durée courte : "45s", "3m 20s", "2h 05m", "∞". */
    public static String duration(float seconds){
        if(Float.isInfinite(seconds) || Float.isNaN(seconds)) return "∞";
        long s = Math.max(0, Math.round(seconds));
        if(s < 60) return s + "s";
        if(s < 3600) return (s / 60) + "m " + String.format(Locale.ROOT, "%02d", s % 60) + "s";
        return (s / 3600) + "h " + String.format(Locale.ROOT, "%02d", (s % 3600) / 60) + "m";
    }

    private static String trim(float v){
        if(v >= 100f || v == Math.rint(v)) return String.valueOf(Math.round(v));
        return String.format(Locale.ROOT, "%.1f", v);
    }
}
