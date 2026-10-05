package minclaude.logic;

import minclaude.logic.SquadPlanner.*;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class V2LogicTest{
    private static final Tactics HARD = Tactics.forDifficulty(3, true);

    private static List<Member> line(int n, float x0, float y, float spacing){
        List<Member> list = new ArrayList<>();
        for(int i = 0; i < n; i++) list.add(new Member(i + 1, x0, y + i * spacing, 1f));
        return list;
    }

    @Test
    void tacticsDisabledOnEasyOrWithoutSmartAi(){
        assertFalse(Tactics.forDifficulty(1, true).enabled());
        assertFalse(Tactics.forDifficulty(3, false).enabled());
        assertTrue(new SquadPlanner().plan(line(5, 0, 0, 1), 50, 0, Tactics.forDifficulty(1, true), 0).isEmpty());
    }

    @Test
    void smallSquadGathersThenAttacksAfterTimeout(){
        SquadPlanner p = new SquadPlanner();
        var orders = p.plan(line(2, 0, 0, 2), 100, 0, HARD, 0);
        assertEquals(OrderType.GATHER, orders.get(1).type());
        assertEquals(0f, orders.get(1).x(), 1e-4, "point de ralliement = centre de l'escouade");
        assertEquals(1f, orders.get(1).y(), 1e-4);
        var later = p.plan(line(2, 0, 0, 2), 100, 0, HARD, HARD.maxGatherSeconds() + 1);
        assertNotEquals(OrderType.GATHER, later.get(1).type(), "attente bornée");
    }

    @Test
    void fullSquadFlanksOnBothSides(){
        // 6 unités alignées perpendiculairement à la cible (cible à droite, ligne verticale).
        var orders = new SquadPlanner().plan(line(6, 0, 0, 1), 100, 2.5f, HARD, 0);
        long flank = orders.values().stream().filter(o -> o.type() == OrderType.FLANK).count();
        long center = orders.values().stream().filter(o -> o.type() == OrderType.NONE).count();
        assertEquals(4, flank, "deux ailes de 2 unités");
        assertEquals(2, center);
        // Les deux ailes visent des côtés opposés de la ligne d'attaque.
        var ys = orders.values().stream().filter(o -> o.type() == OrderType.FLANK).mapToDouble(Order::y).sorted().toArray();
        assertTrue(ys[0] < 2.5f - HARD.flankOffset() / 2 && ys[ys.length - 1] > 2.5f + HARD.flankOffset() / 2);
        // Et elles sont en avant du groupe, vers la cible.
        assertTrue(orders.values().stream().filter(o -> o.type() == OrderType.FLANK).allMatch(o -> o.x() > 0));
    }

    @Test
    void damagedUnitRetreatsForAWhile(){
        SquadPlanner p = new SquadPlanner();
        List<Member> squad = new ArrayList<>(line(5, 0, 0, 1));
        squad.set(2, new Member(3, 0, 2, 0.1f));
        var orders = p.plan(squad, 100, 2, HARD, 0);
        assertEquals(OrderType.RETREAT, orders.get(3).type());
        assertTrue(orders.get(3).x() < 0, "recule à l'opposé de la cible");
        // Toujours en retraite pendant la durée, même soignée.
        squad.set(2, new Member(3, 0, 2, 1f));
        assertEquals(OrderType.RETREAT, p.plan(squad, 100, 2, HARD, HARD.retreatSeconds() - 1).get(3).type());
        assertNotEquals(OrderType.RETREAT, p.plan(squad, 100, 2, HARD, HARD.retreatSeconds() + 1).get(3).type());
    }

    @Test
    void distantUnitsFormSeparateSquads(){
        List<Member> m = new ArrayList<>(line(3, 0, 0, 1));
        m.addAll(List.of(new Member(10, 100, 100, 1), new Member(11, 101, 100, 1), new Member(12, 102, 100, 1)));
        assertEquals(2, SquadPlanner.cluster(m, HARD.squadRadius()).size());
    }

    @Test
    void difficultyMultipliers(){
        assertEquals(new DifficultyProfile(1f, 1f), DifficultyProfile.forDifficulty(2));
        assertTrue(DifficultyProfile.forDifficulty(1).unitHealth() < 1f);
        assertTrue(DifficultyProfile.forDifficulty(4).unitHealth() > DifficultyProfile.forDifficulty(3).unitHealth());
        assertEquals(DifficultyProfile.forDifficulty(4), DifficultyProfile.forDifficulty(10));
    }

    @Test
    void productionModel(){
        ProductionModel p = new ProductionModel();
        p.produces("steel", 0.8f, 1f);
        p.produces("steel", 0.8f, 0.5f);
        p.consumes("scrap", 1.6f, 1f);
        var steel = p.flow("steel");
        assertEquals(1.6f, steel.installedOut, 1e-5);
        assertEquals(1.2f, steel.actualOut, 1e-5);
        assertEquals(0.75f, steel.utilization(), 1e-5);
        assertEquals(-1.6f, p.flow("scrap").installedNet(), 1e-5);
        assertEquals(0f, p.flow("scrap").utilization());
        assertNull(p.flow("gold"));
    }

    @Test
    void goalsFireOnceAndRearm(){
        StockGoals g = new StockGoals();
        g.set("copper", 1000);
        assertFalse(g.check("copper", 500));
        assertEquals(0.5f, g.progress("copper", 500));
        assertTrue(g.check("copper", 1000));
        assertFalse(g.check("copper", 1200), "une seule fois");
        assertFalse(g.check("copper", 950), "pas de réarmement au-dessus de 90 %");
        assertFalse(g.check("copper", 800));
        assertTrue(g.check("copper", 1001), "réarmé après être passé sous 90 %");
        g.set("copper", 0);
        assertFalse(g.has("copper"));
    }

    @Test
    void goalsRoundTripAndRejectGarbage() throws IOException{
        StockGoals g = new StockGoals();
        g.set("copper", 1000);
        g.set("lead", 50);
        g.check("lead", 60);
        var bytes = new ByteArrayOutputStream();
        g.write(new DataOutputStream(bytes));
        StockGoals back = new StockGoals();
        back.read(new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())));
        assertEquals(g.all(), back.all());
        assertFalse(back.check("lead", 70), "l'état « atteint » est conservé");
        assertThrows(IOException.class, () -> new StockGoals().read(new DataInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}))));
    }

    @Test
    void wavePlanV2(){
        var plan = EnemyWavePlan.forDifficulty(2);
        assertEquals(5, plan.size());
        var brute = plan.stream().filter(g -> g.unit().equals("brute")).findFirst().orElseThrow();
        assertTrue(brute.begin() > 30 && brute.max() >= 1, "mini-boss tardif et rare");
    }
}
