package minclaude.logic;

import minclaude.logic.AdaptiveWaves.*;
import minclaude.stats.FlowCounter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class V3LogicTest{
    @Test
    void noAdaptationOnEasyOrWithoutTurrets(){
        assertFalse(AdaptiveWaves.compute(new Defense(10, 0, 10, 100), 1).any());
        assertFalse(AdaptiveWaves.compute(new Defense(0, 0, 0, 50), 4).any(), "début de partie : rien à contrer");
        assertEquals(0, AdaptiveWaves.cap(1));
        assertEquals(3, AdaptiveWaves.cap(4));
        assertEquals(3, AdaptiveWaves.cap(9));
    }

    @Test
    void weakAntiAirBringsFlyers(){
        var a = AdaptiveWaves.compute(new Defense(8, 1, 8, 0), 4);
        assertEquals(3, a.bonus(Family.AIR));
        assertEquals(2, AdaptiveWaves.compute(new Defense(8, 3, 8, 0), 4).bonus(Family.AIR), "moitié du plafond entre 25 et 50 %");
        assertEquals(0, AdaptiveWaves.compute(new Defense(8, 6, 8, 0), 4).bonus(Family.AIR));
    }

    @Test
    void wallsBringSiegeAndWeakGroundBringsSwarms(){
        assertEquals(2, AdaptiveWaves.compute(new Defense(4, 4, 4, 40), 3).bonus(Family.SIEGE));
        assertEquals(1, AdaptiveWaves.compute(new Defense(4, 4, 4, 20), 3).bonus(Family.SIEGE));
        assertEquals(2, AdaptiveWaves.compute(new Defense(4, 4, 1, 0), 3).bonus(Family.SWARM));
        assertEquals(1, AdaptiveWaves.compute(new Defense(14, 14, 14, 0), 3).bonus(Family.ARMOR), "défense lourde → blindés");
    }

    @Test
    void defenseReportProfile(){
        DefenseReport d = new DefenseReport();
        d.addTurret(true, 1f, true, true);
        d.addTurret(true, 1f, false, true);
        d.addWall();
        d.addWall();
        assertEquals(new Defense(2, 1, 2, 2), d.profile());
    }

    @Test
    void exactFlowsReconcileWithStock(){
        // 30 entrées, le stock a baissé de 10 : 40 sorties.
        assertEquals(new FlowCounter.Flows(30, 40), FlowCounter.reconcile(30, -10));
        // 30 entrées, stock +30 : rien n'est sorti.
        assertEquals(new FlowCounter.Flows(30, 0), FlowCounter.reconcile(30, 30));
        // Stock +50 avec seulement 30 comptées : 20 sont arrivées par un autre chemin, comptées en entrée.
        assertEquals(new FlowCounter.Flows(50, 0), FlowCounter.reconcile(30, 50));
    }

    @Test
    void flowCounterAccumulatesAndResets(){
        FlowCounter c = new FlowCounter();
        c.record(3, 10, 0);
        c.record(3, 5, 2);
        c.record(200, 1, 1);
        assertEquals(15, c.takeIn(3));
        assertEquals(2, c.takeLost(3));
        assertEquals(0, c.takeIn(3), "remis à zéro après lecture");
        assertEquals(1, c.takeIn(200));
        c.record(3, 4, 4);
        c.reset();
        assertEquals(0, c.takeLost(3));
        c.record(1, -5, -2);
        assertEquals(0, c.takeIn(1), "jamais négatif");
    }

    @Test
    void wavePlanV3HasBossesLate(){
        var plan = EnemyWavePlan.forDifficulty(2);
        assertEquals(16, plan.size());
        var bosses = plan.stream().filter(EnemyWavePlan.Group::boss).toList();
        assertEquals(2, bosses.size());
        assertTrue(bosses.stream().allMatch(b -> b.begin() >= 60 && b.max() == 1));
        assertTrue(plan.stream().map(EnemyWavePlan.Group::unit).distinct().count() == plan.size(), "un groupe par ennemi");
    }
}
