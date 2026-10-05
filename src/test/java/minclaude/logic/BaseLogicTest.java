package minclaude.logic;

import minclaude.stats.AlertEngine;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BaseLogicTest{
    @Test
    void powerCountsEachNetworkOnce(){
        PowerAggregator p = new PowerAggregator();
        assertTrue(p.add(1, 2f, 1f, 100f, 1000f));
        assertFalse(p.add(1, 2f, 1f, 100f, 1000f), "même réseau vu par un autre bâtiment");
        p.add(2, 0f, 1f, 0f, 0f);
        assertEquals(2, p.networks());
        assertEquals(120f, p.producedPerSecond(), 1e-4);
        assertEquals(120f, p.consumedPerSecond(), 1e-4);
        assertEquals(1f, p.satisfaction());
        assertEquals(0f, p.balancePerSecond(), 1e-4);
        assertEquals(1000f, p.capacity());
    }

    @Test
    void powerSatisfaction(){
        PowerAggregator p = new PowerAggregator();
        assertEquals(1f, p.satisfaction(), "pas de demande");
        p.add(1, 1f, 4f, 0f, 0f);
        assertEquals(0.25f, p.satisfaction(), 1e-5);
        assertTrue(p.balancePerSecond() < 0);
    }

    @Test
    void defenseReport(){
        DefenseReport d = new DefenseReport();
        d.addTurret(true, 1f);
        d.addTurret(false, 0.3f);
        d.addUnit("dagger");
        d.addUnit("dagger");
        d.addUnit("flare");
        d.setWave(5, 42f, 3);
        d.addNextWaveUnits("dagger", 4);
        d.addNextWaveUnits("flare", 0);

        assertEquals(2, d.turrets());
        assertEquals(1, d.turretsNoAmmo());
        assertEquals(1, d.turretsDamaged());
        assertEquals(3, d.unitTotal());
        assertEquals(Map.entry("dagger", 2), d.units().get(0));
        assertEquals(1, d.nextWave().size(), "une ligne à 0 unité est ignorée");
        assertEquals(4, d.nextWaveTotal());
        assertEquals(42f, d.secondsToNextWave());
        assertEquals(3, d.enemiesAlive());
    }

    @Test
    void genericAlertCondition(){
        AlertEngine e = new AlertEngine();
        assertEquals(1, e.evaluateCondition("power", AlertEngine.Type.POWER_SHORTAGE, true, 0.4f, 0).size());
        assertTrue(e.evaluateCondition("power", AlertEngine.Type.POWER_SHORTAGE, true, 0.4f, 1).isEmpty());
        assertTrue(e.isActive("power", AlertEngine.Type.POWER_SHORTAGE));
        assertTrue(e.evaluateCondition("coal", AlertEngine.Type.INDUSTRY_STARVED, false, 0, 2).isEmpty());
    }
}
