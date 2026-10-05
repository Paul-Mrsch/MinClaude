package minclaude.logic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static minclaude.logic.IndustryStatus.*;
import static org.junit.jupiter.api.Assertions.*;

class IndustryTest{
    @Test
    void classifyCoversEveryCase(){
        assertEquals(DISABLED, IndustryStatus.classify(false, true, 1f, true, 1f, false));
        assertEquals(NO_OUTPUT, IndustryStatus.classify(true, false, 0f, true, 1f, false));
        assertEquals(ACTIVE, IndustryStatus.classify(true, true, 0.3f, true, 0.3f, false));
        assertEquals(NO_INPUT, IndustryStatus.classify(true, true, 0f, true, 1f, true), "ressource manquante prioritaire");
        assertEquals(NO_POWER, IndustryStatus.classify(true, true, 0f, true, 0f, false));
        assertEquals(NO_INPUT, IndustryStatus.classify(true, true, 0f, false, 1f, false), "ex. liquide manquant");
        assertFalse(ACTIVE.blocked());
        assertFalse(DISABLED.blocked());
        assertTrue(NO_POWER.blocked());
    }

    @Test
    void reportAggregatesPerBlock(){
        IndustryReport r = new IndustryReport();
        r.add("smelter", ACTIVE, 1f, List.of());
        r.add("smelter", NO_INPUT, 0f, List.of("coal"));
        r.add("smelter", NO_POWER, 0f, List.of());
        r.add("press", ACTIVE, 0.5f, List.of());

        assertEquals(4, r.total());
        assertEquals(2, r.blocked());
        IndustryReport.BlockSummary s = r.block("smelter");
        assertEquals(3, s.total());
        assertEquals(1, s.count(NO_INPUT));
        assertEquals(2, s.blocked());
        assertEquals(1f / 3f, s.averageEfficiency(), 1e-5);
        assertEquals("smelter", r.blocks().get(0).block, "le plus bloqué en premier");
    }

    @Test
    void bottlenecksRankMissingItems(){
        IndustryReport r = new IndustryReport();
        r.add("a", NO_INPUT, 0f, List.of("coal", "sand"));
        r.add("b", NO_INPUT, 0f, List.of("coal"));
        r.add("c", NO_POWER, 0f, List.of("lead")); // pas compté : le blocage est l'énergie
        var list = r.bottlenecks();
        assertEquals(2, list.size());
        assertEquals(new IndustryReport.Bottleneck("coal", 2), list.get(0));
        assertEquals(1, r.starvedBy("sand"));
        assertEquals(0, r.starvedBy("lead"));
    }

    @Test
    void efficiencyIsClamped(){
        IndustryReport r = new IndustryReport();
        r.add("x", ACTIVE, 3f, List.of());
        assertEquals(1f, r.block("x").averageEfficiency());
    }
}
