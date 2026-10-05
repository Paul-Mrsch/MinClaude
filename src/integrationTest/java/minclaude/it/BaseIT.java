package minclaude.it;

import arc.util.Time;
import minclaude.MinClaudeMod;
import minclaude.content.MCBlocks;
import minclaude.content.MCItems;
import minclaude.logic.IndustryStatus;
import minclaude.stats.TimeRange;
import minclaude.tracking.BaseScanner;
import mindustry.content.*;
import mindustry.core.GameState.State;
import mindustry.gen.Building;
import org.junit.jupiter.api.*;

import static minclaude.it.HeadlessGame.*;
import static mindustry.Vars.*;
import static org.junit.jupiter.api.Assertions.*;

/** Énergie, usines et défense relevées dans une vraie partie : bâtiments posés puis simulation complète (logic.update). */
class BaseIT{
    @BeforeAll
    static void boot(){
        HeadlessGame.start();
    }

    @BeforeEach
    void newGame(){
        Time.setDeltaProvider(() -> 1f);
        logic.reset();
        world.loadMap(groundZero);
        state.set(State.playing);
    }

    @Test
    void starvedSmelterIsABottleneckAndPowerIsRecorded(){
        var at = freeArea(3, 2);
        Building smelter = place(MCBlocks.aluminumSmelter, state.rules.defaultTeam, at.x, at.y);
        place(Blocks.solarPanel, state.rules.defaultTeam, at.x + 2, at.y);
        run(150);

        var base = MinClaudeMod.tracker.base();
        var summary = base.industry().block(MCBlocks.aluminumSmelter.name);
        assertNotNull(summary, "usine relevée");
        assertEquals(1, summary.count(IndustryStatus.NO_INPUT));
        assertEquals(1, base.industry().starvedBy(MCItems.bauxite.name));
        assertEquals(1, base.industry().starvedBy(Items.coal.name));
        var missing = base.industry().bottlenecks().stream().map(b -> b.item()).toList();
        assertTrue(missing.containsAll(java.util.List.of(MCItems.bauxite.name, Items.coal.name)), "goulots : " + missing);

        assertTrue(base.power().networks() >= 1);
        assertTrue(base.power().producedPerSecond() > 0f, "le panneau solaire produit");
        assertFalse(MinClaudeMod.tracker.history().view(BaseScanner.POWER_PRODUCED, TimeRange.MINUTE).isEmpty(), "énergie historisée");

        // On approvisionne l'usine : elle repart.
        smelter.items.add(MCItems.bauxite, 10);
        smelter.items.add(Items.coal, 10);
        run(120);
        var after = MinClaudeMod.tracker.base().industry().block(MCBlocks.aluminumSmelter.name);
        assertEquals(1, after.count(IndustryStatus.ACTIVE), "usine active une fois approvisionnée");
        assertEquals(0, MinClaudeMod.tracker.base().industry().starvedBy(MCItems.bauxite.name));
    }

    @Test
    void unpoweredSmelterIsReportedAsNoPower(){
        var at = freeArea(2, 2);
        Building smelter = place(MCBlocks.brassFoundry, state.rules.defaultTeam, at.x, at.y);
        smelter.items.add(Items.copper, 10);
        smelter.items.add(MCItems.zinc, 10);
        run(120);
        assertEquals(1, MinClaudeMod.tracker.base().industry().block(MCBlocks.brassFoundry.name).count(IndustryStatus.NO_POWER));
    }

    @Test
    void defenseCountsTurretsUnitsAndWave(){
        var at = freeArea(1, 1);
        place(Blocks.duo, state.rules.defaultTeam, at.x, at.y);
        UnitTypes.dagger.spawn(state.rules.defaultTeam, at.worldx() + 40f, at.worldy());
        run(120);

        var d = MinClaudeMod.tracker.base().defense();
        assertEquals(1, d.turrets());
        assertEquals(1, d.turretsNoAmmo(), "duo posé sans munitions");
        assertTrue(d.units().stream().anyMatch(e -> e.getKey().equals(UnitTypes.dagger.name)));
        assertTrue(state.rules.waves);
        assertEquals(state.wave, d.wave());
        if(state.rules.waveTimer) assertEquals(state.wavetime / 60f, d.secondsToNextWave(), 0.5f);
        else assertEquals(Float.POSITIVE_INFINITY, d.secondsToNextWave(), "pas de compte à rebours : vague à la demande");
        assertTrue(d.nextWaveTotal() > 0, "Ground Zero : la première vague contient des unités");
    }
}
