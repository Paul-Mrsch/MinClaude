package minclaude.it;

import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.util.Log;
import arc.util.Time;
import minclaude.MinClaudeMod;
import minclaude.ModSettings;
import minclaude.ai.SmartAI;
import minclaude.ai.WaveAdapter;
import minclaude.content.MCUnits;
import minclaude.logic.AdaptiveWaves.Family;
import minclaude.logic.SquadPlanner.Tactics;
import minclaude.logic.TargetScorer;
import minclaude.stats.*;
import minclaude.tracking.CoreFlowHook;
import minclaude.world.WorldSetup;
import mindustry.content.*;
import mindustry.core.GameState.State;
import mindustry.game.EventType.*;
import mindustry.io.SaveIO;
import org.junit.jupiter.api.*;

import static minclaude.it.HeadlessGame.*;
import static mindustry.Vars.*;
import static org.junit.jupiter.api.Assertions.*;

/** V3 dans le vrai jeu : mesure exacte des flux du noyau, adaptation des vagues, performance, partie longue. */
class V3IT{
    @BeforeAll
    static void boot(){
        HeadlessGame.start();
    }

    @BeforeEach
    void newGame(){
        Core.settings.put(ModSettings.DIFFICULTY, 4);
        Core.settings.put(ModSettings.SMART_AI, true);
        Core.settings.put(ModSettings.ENEMIES, true);
        Time.setDeltaProvider(() -> 1f);
        logic.reset();
        world.loadMap(groundZero);
        state.set(State.playing);
    }

    @AfterEach
    void restore(){
        Core.settings.put(ModSettings.DIFFICULTY, 2);
        MinClaudeMod.squads.setTactics(null);
        SmartAI.setProfile(null);
    }

    private static void tick(int n){
        for(int i = 0; i < n; i++) Events.fire(Trigger.update);
    }

    @Test
    void coreIsInstrumentedAndFlowsAreExact(){
        var core = MinClaudeMod.tracker.core();
        assertInstanceOf(CoreFlowHook.TrackedCoreBuild.class, core, "le noyau vanilla est instrumenté");
        assertTrue(MinClaudeMod.tracker.exactFlows());
        core.items.set(Items.copper, 1000);
        tick(60); // première seconde : référence du stock

        // Seconde suivante : 40 entrent par le noyau et 25 sortent dans le même tick (impossible à voir par l'estimation).
        tick(1);
        core.handleStack(Items.copper, 40, null);
        core.items.remove(Items.copper, 25);
        tick(59);

        var h = MinClaudeMod.tracker.history();
        float in = h.get(ResourceStats.inKey(Items.copper.name)).latest(), out = h.get(ResourceStats.outKey(Items.copper.name)).latest();
        assertEquals(40f, in, "entrées exactes");
        assertEquals(25f, out, "sorties exactes, même dans le même tick que les entrées");
    }

    @Test
    void itemsLostWhenCoreIsFullAreCounted(){
        var core = MinClaudeMod.tracker.core();
        core.items.set(Items.lead, core.storageCapacity);
        tick(60);
        tick(1);
        core.handleStack(Items.lead, 30, null);
        for(int i = 0; i < 5; i++) core.handleItem(core, Items.lead);
        tick(59);
        var lost = MinClaudeMod.tracker.history().get(ResourceStats.lostKey(Items.lead.name));
        assertNotNull(lost);
        assertEquals(35f, lost.latest(), "objets détruits faute de place");
        assertEquals(0f, MinClaudeMod.tracker.history().get(ResourceStats.inKey(Items.lead.name)).latest());
    }

    @Test
    void wavesAdaptToDefenses(){
        Events.fire(Trigger.newGame);
        // Défense sans anti-aérien et avec beaucoup de murs : hail (anti-sol seulement) + murs.
        var at = freeArea(12, 6);
        for(int i = 0; i < 4; i++) place(Blocks.hail, state.rules.defaultTeam, at.x + i, at.y);
        for(int i = 0; i < 12; i++) for(int j = 2; j < 5; j++) place(Blocks.copperWall, state.rules.defaultTeam, at.x + i, at.y + j);
        var air = state.rules.spawns.find(g -> g.type == MCUnits.phantom);
        int before = air.unitAmount;

        MinClaudeMod.waves.adapt();
        var a = MinClaudeMod.waves.current();
        assertEquals(3, a.bonus(Family.AIR), "Brutal, aucun anti-aérien : +3 volants");
        assertEquals(3, a.bonus(Family.SIEGE), "36 murs pour 4 tourelles : +3 unités de siège");
        assertEquals(before + 3, air.unitAmount);

        // On ajoute de l'anti-aérien : le bonus disparaît, sans dériver la valeur de base.
        // Duo : tourelle 1x1 qui vise air et sol.
        for(int i = 0; i < 8; i++) place(Blocks.duo, state.rules.defaultTeam, at.x + i, at.y + 1);
        MinClaudeMod.waves.adapt();
        assertEquals(0, MinClaudeMod.waves.current().bonus(Family.AIR));
        assertEquals(before, air.unitAmount, "retour exact à la valeur de base");
        assertEquals(Family.AIR, WaveAdapter.family(MCUnits.leviathan));
    }

    @Test
    void noAdaptationOnEasy(){
        Core.settings.put(ModSettings.DIFFICULTY, 1);
        Events.fire(Trigger.newGame);
        var at = freeArea(4, 1);
        for(int i = 0; i < 4; i++) place(Blocks.hail, state.rules.defaultTeam, at.x + i, at.y);
        MinClaudeMod.waves.adapt();
        assertFalse(MinClaudeMod.waves.current().any());
    }

    @Test
    void largeBasePerformance(){
        // Grande base : une zone remplie de bâtiments 1x1 variés (tourelles, convoyeurs, murs, générateurs) et 150 unités ennemies.
        var at = freeArea(16, 10);
        mindustry.world.Block[] mix = {Blocks.duo, Blocks.conveyor, Blocks.copperWall, Blocks.combustionGenerator, Blocks.battery};
        int placed = 0;
        for(int x = 0; x < 16; x++){
            for(int y = 0; y < 10; y++){
                place(mix[(x * 7 + y) % mix.length], state.rules.defaultTeam, at.x + x, at.y + y);
                placed++;
            }
        }
        for(int i = 0; i < 150; i++) UnitTypes.dagger.spawn(state.rules.waveTeam, at.worldx() + (i % 15) * 8f, at.worldy() - 80f - (i / 15) * 8f);
        MinClaudeMod.squads.setTactics(Tactics.forDifficulty(4, true));
        SmartAI.setProfile(TargetScorer.Profile.forDifficulty(4, true));
        tick(120); // chauffe

        int seconds = 20;
        long t0 = System.nanoTime();
        tick(60 * seconds);
        double perTickMs = (System.nanoTime() - t0) / 1e6 / (60.0 * seconds);

        long t1 = System.nanoTime();
        for(int i = 0; i < 20; i++){
            MinClaudeMod.tracker.scanNow();
            MinClaudeMod.squads.replan();
        }
        double perSecondMs = (System.nanoTime() - t1) / 1e6 / 20;
        Log.info("[MinClaude perf] @ bâtiments, 150 unités : @ ms par tick (tracker), @ ms par seconde (scanner + escouades)",
            placed, String.format("%.4f", perTickMs), String.format("%.3f", perSecondMs));
        // Budget : 16,6 ms par image ; le mod doit rester négligeable.
        assertTrue(perTickMs < 0.5, "suivi par tick trop coûteux : " + perTickMs + " ms");
        assertTrue(perSecondMs < 5, "relevé + escouades trop coûteux : " + perSecondMs + " ms");
    }

    @Test
    void longGameKeepsMemoryAndSaveBounded(){
        var core = MinClaudeMod.tracker.core();
        for(var item : content.items()) if(!item.isHidden()) core.items.set(item, 100);
        // Une heure de jeu.
        for(int s = 0; s < 3600; s++){
            core.items.add(Items.copper, 1);
            tick(60);
        }
        var h = MinClaudeMod.tracker.history();
        assertEquals(3600, h.elapsedSeconds());
        int bytes;
        try{
            bytes = HistoryCodec.toBytes(h).length;
        }catch(java.io.IOException e){
            throw new AssertionError(e);
        }
        Fi file = new Fi(System.getProperty("java.io.tmpdir")).child("minclaude-long.msav");
        SaveIO.save(file);
        Log.info("[MinClaude perf] 1 h, @ séries : historique @ Ko, sauvegarde @ Ko", h.size(), bytes / 1024, file.length() / 1024);
        assertTrue(bytes < 3_000_000, "historique trop volumineux : " + bytes);
        assertTrue(file.length() < 2_000_000, "sauvegarde trop volumineuse : " + file.length());
        // Les séries restent bornées : 10 min à la seconde, 1 h à 10 s, toute la partie compactée.
        var copper = h.get(ResourceStats.stockKey(Items.copper.name));
        assertEquals(600, copper.view(TimeRange.TEN_MINUTES).values().length);
        assertEquals(360, copper.view(TimeRange.HOUR).values().length);
        assertTrue(copper.view(TimeRange.WHOLE_GAME).values().length <= 720);
    }
}
