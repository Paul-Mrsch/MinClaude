package minclaude.stats;

/** Plages de temps proposées dans l'interface. */
public enum TimeRange{
    MINUTE(60),
    TEN_MINUTES(600),
    HOUR(3600),
    WHOLE_GAME(-1);

    /** Durée en secondes, -1 = toute la partie. */
    public final int seconds;

    TimeRange(int seconds){
        this.seconds = seconds;
    }
}
