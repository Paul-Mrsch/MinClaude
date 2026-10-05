package minclaude.logic;

import java.util.*;

/**
 * IA ennemie, tactiques de groupe. Regroupe les unités proches en escouades, puis :
 * <ul>
 *     <li><b>regroupement</b> : une escouade trop petite se rassemble sur son centre avant d'attaquer,
 *     dans la limite d'un temps d'attente ;</li>
 *     <li><b>flanquement</b> : à l'assaut, les unités des ailes visent un point décalé sur le côté de la ligne d'attaque ;</li>
 *     <li><b>retraite</b> : une unité très abîmée recule derrière son escouade quelques secondes.</li>
 * </ul>
 * Logique pure : positions en cases, temps en secondes. Les ordres sont recalculés chaque seconde.
 */
public final class SquadPlanner{
    public enum OrderType{ NONE, GATHER, FLANK, RETREAT }

    public record Order(OrderType type, float x, float y){
        public static final Order NONE = new Order(OrderType.NONE, 0, 0);
    }

    public record Member(int id, float x, float y, float health){}

    /** Paramètres selon la difficulté. */
    public record Tactics(boolean enabled, float squadRadius, int minSquad, float maxGatherSeconds, float flankOffset,
                          float flankReach, float retreatHealth, float retreatSeconds){
        public static Tactics forDifficulty(int difficulty, boolean smartAi){
            if(!smartAi || difficulty <= 1) return new Tactics(false, 0, 0, 0, 0, 0, 0, 0);
            return switch(difficulty){
                case 2 -> new Tactics(true, 10f, 3, 10f, 6f, 10f, 0f, 0f);
                case 3 -> new Tactics(true, 12f, 4, 15f, 8f, 12f, 0.3f, 5f);
                default -> new Tactics(true, 14f, 5, 20f, 10f, 14f, 0.35f, 6f);
            };
        }
    }

    private final Map<Integer, Float> squadSince = new HashMap<>();
    private final Map<Integer, Float> retreatUntil = new HashMap<>();
    private final Map<Integer, Order> orders = new HashMap<>();

    /**
     * @param targetX, targetY objectif de l'attaque (noyau du joueur), en cases
     * @param now temps de jeu en secondes
     */
    public Map<Integer, Order> plan(List<Member> members, float targetX, float targetY, Tactics t, float now){
        orders.clear();
        if(!t.enabled() || members.isEmpty()) return orders;

        List<List<Member>> squads = cluster(members, t.squadRadius());
        Set<Integer> alive = new HashSet<>();
        for(List<Member> squad : squads){
            int key = squad.stream().mapToInt(Member::id).min().orElse(0);
            alive.add(key);
            float since = squadSince.computeIfAbsent(key, k -> now);
            float cx = 0, cy = 0;
            for(Member m : squad){
                cx += m.x();
                cy += m.y();
            }
            cx /= squad.size();
            cy /= squad.size();

            boolean gathering = squad.size() < t.minSquad() && now - since < t.maxGatherSeconds();
            float dx = targetX - cx, dy = targetY - cy, len = (float)Math.max(1e-3, Math.hypot(dx, dy));
            float fx = dx / len, fy = dy / len, px = -fy, py = fx;

            // Rangs latéraux : projection sur la perpendiculaire à la ligne d'attaque.
            List<Member> byLateral = new ArrayList<>(squad);
            final float ccx = cx, ccy = cy;
            byLateral.sort(Comparator.comparingDouble(m -> (m.x() - ccx) * px + (m.y() - ccy) * py));
            int third = squad.size() / 3;

            for(int i = 0; i < byLateral.size(); i++){
                Member m = byLateral.get(i);
                Float until = retreatUntil.get(m.id());
                if(until != null && now < until){
                    orders.put(m.id(), new Order(OrderType.RETREAT, cx - fx * t.flankReach(), cy - fy * t.flankReach()));
                    continue;
                }
                if(t.retreatHealth() > 0 && m.health() < t.retreatHealth() && squad.size() > 1 && until == null){
                    retreatUntil.put(m.id(), now + t.retreatSeconds());
                    orders.put(m.id(), new Order(OrderType.RETREAT, cx - fx * t.flankReach(), cy - fy * t.flankReach()));
                    continue;
                }
                if(gathering){
                    orders.put(m.id(), new Order(OrderType.GATHER, cx, cy));
                }else if(third > 0 && (i < third || i >= squad.size() - third)){
                    float side = i < third ? -1f : 1f;
                    orders.put(m.id(), new Order(OrderType.FLANK,
                        cx + fx * t.flankReach() + px * side * t.flankOffset(), cy + fy * t.flankReach() + py * side * t.flankOffset()));
                }else{
                    orders.put(m.id(), Order.NONE);
                }
            }
        }
        squadSince.keySet().retainAll(alive);
        Set<Integer> ids = new HashSet<>();
        for(Member m : members) ids.add(m.id());
        retreatUntil.keySet().retainAll(ids);
        return orders;
    }

    /** Escouades : composantes connexes des unités à moins de {@code radius} cases les unes des autres. */
    static List<List<Member>> cluster(List<Member> members, float radius){
        int n = members.size();
        int[] parent = new int[n];
        for(int i = 0; i < n; i++) parent[i] = i;
        float r2 = radius * radius;
        for(int i = 0; i < n; i++){
            for(int j = i + 1; j < n; j++){
                float dx = members.get(i).x() - members.get(j).x(), dy = members.get(i).y() - members.get(j).y();
                if(dx * dx + dy * dy <= r2) union(parent, i, j);
            }
        }
        Map<Integer, List<Member>> groups = new LinkedHashMap<>();
        for(int i = 0; i < n; i++) groups.computeIfAbsent(find(parent, i), k -> new ArrayList<>()).add(members.get(i));
        return new ArrayList<>(groups.values());
    }

    private static int find(int[] p, int i){
        while(p[i] != i){
            p[i] = p[p[i]];
            i = p[i];
        }
        return i;
    }

    private static void union(int[] p, int a, int b){
        p[find(p, a)] = find(p, b);
    }
}
