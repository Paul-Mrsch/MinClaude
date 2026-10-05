package minclaude.logic;

import java.util.*;

/**
 * Capacité de production installée par ressource : ce que les usines construites produiraient et consommeraient
 * à plein régime, et ce qu'elles font réellement (pondéré par leur rendement). Valeurs par seconde.
 */
public final class ProductionModel{
    public static final ProductionModel EMPTY = new ProductionModel();

    public static final class Flow{
        public float installedOut, actualOut, installedIn, actualIn;

        /** Part de la capacité de production réellement utilisée (0..1), 0 sans capacité. */
        public float utilization(){
            return installedOut <= 0f ? 0f : Math.min(1f, actualOut / installedOut);
        }

        public float installedNet(){
            return installedOut - installedIn;
        }
    }

    private final Map<String, Flow> flows = new HashMap<>();

    /** Une usine produit {@code perSecond} de {@code item} à plein régime, avec le rendement {@code efficiency}. */
    public void produces(String item, float perSecond, float efficiency){
        Flow f = flows.computeIfAbsent(item, k -> new Flow());
        f.installedOut += perSecond;
        f.actualOut += perSecond * clamp(efficiency);
    }

    public void consumes(String item, float perSecond, float efficiency){
        Flow f = flows.computeIfAbsent(item, k -> new Flow());
        f.installedIn += perSecond;
        f.actualIn += perSecond * clamp(efficiency);
    }

    public Flow flow(String item){
        return flows.get(item);
    }

    public Set<String> items(){
        return Collections.unmodifiableSet(flows.keySet());
    }

    private static float clamp(float v){
        return Math.max(0f, Math.min(1f, v));
    }
}
