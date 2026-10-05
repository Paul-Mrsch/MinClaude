package minclaude.it;

import arc.Events;
import arc.files.Fi;
import arc.util.Time;
import minclaude.MinClaudeMod;
import minclaude.stats.*;
import minclaude.tracking.*;
import mindustry.content.Items;
import mindustry.core.GameState.State;
import mindustry.game.EventType.Trigger;
import mindustry.io.SaveIO;
import mindustry.world.blocks.storage.CoreBlock.CoreBuild;
import org.junit.jupiter.api.*;

import java.io.*;

import static mindustry.Vars.*;
import static org.junit.jupiter.api.Assertions.*;

/** Suivi des ressources dans une vraie partie headless, et persistance de l'historique dans la sauvegarde. */
class TrackingIT{
    private static final ResourceTracker tracker = MinClaudeMod.tracker;

    @BeforeAll
    static void boot(){
        HeadlessGame.start();
    }

    @BeforeEach
    void newGame(){
        Time.setDeltaProvider(() -> 1f);
        logic.reset();
        world.loadMap(HeadlessGame.groundZero);
        state.set(State.playing);
    }

    private static CoreBuild core(){
        CoreBuild core = tracker.core();
        assertNotNull(core, "groundZero doit avoir un noyau");
        return core;
    }

    /** Un tick de jeu côté tracker (le reste de la logique n'est pas nécessaire). */
    private static void ticks(int n, Runnable eachTick){
        for(int i = 0; i < n; i++){
            eachTick.run();
            Events.fire(Trigger.update);
        }
    }

    @Test
    void recordsStockInAndOutEverySecond(){
        CoreBuild core = core();
        core.items.set(Items.copper, 1000);
        Events.fire(Trigger.update); // tick 1 : première observation, ce n'est pas une entrée
        ticks(59, () -> core.items.add(Items.copper, 3)); // ticks 2..60 -> 1re seconde
        ticks(60, () -> core.items.remove(Items.copper, 1)); // ticks 61..120 -> 2e seconde

        MetricHistory h = tracker.history();
        assertEquals(2, h.elapsedSeconds());
        float[] stock = h.view(ResourceStats.stockKey(Items.copper.name), TimeRange.MINUTE).values();
        float[] in = h.view(ResourceStats.inKey(Items.copper.name), TimeRange.MINUTE).values();
        float[] out = h.view(ResourceStats.outKey(Items.copper.name), TimeRange.MINUTE).values();
        assertArrayEquals(new float[]{1177, 1117}, stock);
        assertArrayEquals(new float[]{177, 0}, in);
        assertArrayEquals(new float[]{0, 60}, out);
        assertTrue(tracker.isTracked(Items.copper));
    }

    @Test
    void pausedGameIsNotRecorded(){
        state.set(State.paused);
        ticks(300, () -> {});
        assertEquals(0, tracker.history().elapsedSeconds());
    }

    @Test
    void newWorldStartsWithFreshHistory(){
        ticks(120, () -> {});
        assertEquals(2, tracker.history().elapsedSeconds());
        world.loadMap(HeadlessGame.groundZero);
        assertEquals(0, tracker.history().elapsedSeconds());
    }

    @Test
    void historySurvivesSaveAndLoad(){
        CoreBuild core = core();
        core.items.set(Items.lead, 500);
        ticks(600, () -> core.items.add(Items.lead, 1));
        MetricHistory before = tracker.history();
        float[] expected = before.view(ResourceStats.stockKey(Items.lead.name), TimeRange.TEN_MINUTES).values();
        assertEquals(10, before.elapsedSeconds());

        Fi file = new Fi(System.getProperty("java.io.tmpdir")).child("minclaude-it.msav");
        SaveIO.save(file);
        logic.reset();
        assertEquals(0, tracker.history().elapsedSeconds());

        SaveIO.load(file);
        MetricHistory after = tracker.history();
        assertEquals(10, after.elapsedSeconds(), "l'historique doit être relu après WorldLoadEvent");
        assertArrayEquals(expected, after.view(ResourceStats.stockKey(Items.lead.name), TimeRange.TEN_MINUTES).values());
        assertTrue(tracker.isTracked(Items.lead));
    }

    @Test
    void corruptedChunkIsIgnoredWithoutFailing(){
        byte[] garbage = {9, 9, 9, 9, 9, 9, 9, 9, 9, 9};
        var chunk = new HistoryChunk(tracker);
        assertDoesNotThrow(() -> chunk.read(new DataInputStream(new ByteArrayInputStream(garbage)), garbage.length));
        assertEquals(0, tracker.history().size());
    }
}
