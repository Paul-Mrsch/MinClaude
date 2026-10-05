package minclaude.stats;

import java.util.*;

/**
 * Transforme les {@link ResourceStats} en alertes ponctuelles. Une alerte se déclenche au moment où sa condition
 * apparaît, puis ne se redéclenche qu'après la disparition de la condition et l'expiration du délai {@code cooldownSeconds}.
 */
public final class AlertEngine{
    public enum Type{
        /** Le stock baisse et sera épuisé bientôt. */
        DEPLETING,
        /** Le stock est passé sous le seuil bas. */
        LOW_STOCK,
        /** Le noyau est plein alors que des ressources arrivent : elles sont perdues. */
        CORE_FULL
    }

    public record Alert(String item, Type type, float value){}

    public static final class Config{
        /** Seuil bas, en fraction de la capacité du noyau. */
        public float lowFraction = 0.05f;
        /** Alerte d'épuisement si l'échéance est inférieure à ce nombre de secondes. */
        public float depletingSeconds = 180f;
        /** Fraction de capacité considérée comme « plein ». */
        public float fullFraction = 0.98f;
        /** Délai minimum entre deux alertes identiques. */
        public float cooldownSeconds = 120f;
    }

    private static final class State{
        boolean active;
        float lastFired = Float.NEGATIVE_INFINITY;
    }

    public final Config config;
    private final Map<String, State> states = new HashMap<>();
    private final Set<String> seenAboveLow = new HashSet<>();

    public AlertEngine(Config config){
        this.config = config;
    }

    public AlertEngine(){
        this(new Config());
    }

    /** Évalue une ressource à l'instant {@code now} (secondes de jeu) et renvoie les nouvelles alertes. */
    public List<Alert> evaluate(String item, ResourceStats s, float now){
        List<Alert> result = new ArrayList<>(1);
        float low = s.capacity() * config.lowFraction;
        if(s.capacity() > 0 && s.stock() > low) seenAboveLow.add(item);

        boolean depleting = s.stock() > 0 && s.secondsToEmpty() < config.depletingSeconds;
        // N'alerte pas pour une ressource qui n'a jamais été abondante (sinon une alerte par ressource à chaque début de partie).
        boolean lowStock = s.capacity() > 0 && seenAboveLow.contains(item) && s.stock() <= low;
        boolean full = s.capacity() > 0 && s.stock() >= s.capacity() * config.fullFraction && s.inPerSec() > 0;

        check(item, Type.DEPLETING, depleting, s.secondsToEmpty(), now, result);
        check(item, Type.LOW_STOCK, lowStock, s.stock(), now, result);
        check(item, Type.CORE_FULL, full, s.inPerSec(), now, result);
        return result;
    }

    /** Alertes dont la condition est vraie en ce moment, déclenchées ou non. */
    public boolean isActive(String item, Type type){
        State st = states.get(item + "/" + type);
        return st != null && st.active;
    }

    public void reset(){
        states.clear();
        seenAboveLow.clear();
    }

    private void check(String item, Type type, boolean condition, float value, float now, List<Alert> out){
        State st = states.computeIfAbsent(item + "/" + type, k -> new State());
        if(condition && !st.active && now - st.lastFired >= config.cooldownSeconds){
            st.lastFired = now;
            out.add(new Alert(item, type, value));
        }
        st.active = condition;
    }
}
