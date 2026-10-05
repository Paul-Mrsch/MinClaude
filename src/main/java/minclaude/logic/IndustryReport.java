package minclaude.logic;

import java.util.*;

/**
 * Synthèse des usines d'une base à un instant donné : état par type de bloc et goulots d'étranglement
 * (ressources qui manquent au plus grand nombre d'usines).
 */
public final class IndustryReport{
    public static final IndustryReport EMPTY = new IndustryReport();

    /** Résumé d'un type de bloc. {@code counts} est indexé par {@link IndustryStatus#ordinal()}. */
    public static final class BlockSummary{
        public final String block;
        public final int[] counts = new int[IndustryStatus.values().length];
        private float efficiencySum;
        private int total;

        BlockSummary(String block){
            this.block = block;
        }

        public int total(){
            return total;
        }

        public int count(IndustryStatus status){
            return counts[status.ordinal()];
        }

        public int blocked(){
            int n = 0;
            for(IndustryStatus s : IndustryStatus.values()) if(s.blocked()) n += counts[s.ordinal()];
            return n;
        }

        /** Efficacité moyenne de 0 à 1. */
        public float averageEfficiency(){
            return total == 0 ? 0f : efficiencySum / total;
        }
    }

    public record Bottleneck(String item, int starvedBuildings){}

    private final LinkedHashMap<String, BlockSummary> blocks = new LinkedHashMap<>();
    private final HashMap<String, Integer> missing = new HashMap<>();
    private int total, blocked;

    public void add(String block, IndustryStatus status, float efficiency, Collection<String> missingItems){
        BlockSummary s = blocks.computeIfAbsent(block, BlockSummary::new);
        s.counts[status.ordinal()]++;
        s.total++;
        s.efficiencySum += Math.max(0f, Math.min(1f, efficiency));
        total++;
        if(status.blocked()) blocked++;
        if(status == IndustryStatus.NO_INPUT){
            for(String item : missingItems) missing.merge(item, 1, Integer::sum);
        }
    }

    public int total(){
        return total;
    }

    public int blocked(){
        return blocked;
    }

    /** Types de blocs, les plus bloqués en premier puis les plus nombreux. */
    public List<BlockSummary> blocks(){
        List<BlockSummary> list = new ArrayList<>(blocks.values());
        list.sort(Comparator.comparingInt(BlockSummary::blocked).reversed().thenComparing(Comparator.comparingInt(BlockSummary::total).reversed()));
        return list;
    }

    public BlockSummary block(String name){
        return blocks.get(name);
    }

    /** Ressources manquantes, de celle qui bloque le plus d'usines à celle qui en bloque le moins. */
    public List<Bottleneck> bottlenecks(){
        List<Bottleneck> list = new ArrayList<>();
        missing.forEach((item, n) -> list.add(new Bottleneck(item, n)));
        list.sort(Comparator.comparingInt(Bottleneck::starvedBuildings).reversed().thenComparing(Bottleneck::item));
        return list;
    }

    public int starvedBy(String item){
        return missing.getOrDefault(item, 0);
    }
}
