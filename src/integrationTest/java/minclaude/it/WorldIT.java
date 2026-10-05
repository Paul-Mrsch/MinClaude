package minclaude.it;

import arc.Core;
import arc.Events;
import arc.util.Time;
import minclaude.ModSettings;
import minclaude.ai.SmartAI;
import minclaude.ai.SmartGroundAI;
import minclaude.content.MCBlocks;
import minclaude.content.MCUnits;
import minclaude.logic.TargetScorer;
import minclaude.world.WorldSetup;
import mindustry.content.*;
import mindustry.core.GameState.State;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import org.junit.jupiter.api.*;

import java.util.HashSet;
import java.util.Set;

import static minclaude.it.HeadlessGame.*;
import static mindustry.Vars.*;
import static org.junit.jupiter.api.Assertions.*;

/** Nouvelles parties (minerais, vagues ennemies) et IA ennemie intelligente, dans le vrai jeu. */
class WorldIT{
    @BeforeAll
    static void boot(){
        HeadlessGame.start();
    }

    @BeforeEach
    void newGame(){
        Core.settings.put(ModSettings.ORES, true);
        Core.settings.put(ModSettings.ENEMIES, true);
        Core.settings.put(ModSettings.DIFFICULTY, 2);
        Time.setDeltaProvider(() -> 1f);
        logic.reset();
        world.loadMap(groundZero);
        state.set(State.playing);
        SmartAI.setProfile(null);
    }

    private static Set<Integer> modOreTiles(){
        Set<Integer> tiles = new HashSet<>();
        world.tiles.eachTile(t -> {
            if(MCBlocks.ores.contains(o -> o == t.overlay())) tiles.add(t.pos());
        });
        return tiles;
    }

    @Test
    void newGameAddsOresOnceAndDeterministically(){
        assertTrue(modOreTiles().isEmpty(), "Ground Zero n'a pas de minerai du mod à l'origine");
        Events.fire(Trigger.newGame);
        Set<Integer> first = modOreTiles();
        assertFalse(first.isEmpty(), "des gisements ont été ajoutés");
        assertTrue(state.rules.tags.getBool(WorldSetup.MARKER));
        for(int pos : first){
            var t = world.tile(pos);
            assertSame(Blocks.air, t.block(), "jamais sous un bâtiment ou un mur");
            assertFalse(t.floor().isLiquid);
        }

        Events.fire(Trigger.newGame);
        assertEquals(first, modOreTiles(), "pas de seconde application");

        Set<Integer> blockedFirst = occupiedTiles();
        newGame();
        Events.fire(Trigger.newGame);
        Set<Integer> second = modOreTiles(), blockedSecond = occupiedTiles();
        // Le chargement replace quelques rochers décoratifs au hasard. Seules ces cases peuvent différer :
        // ailleurs, la répartition doit être identique (déterminisme exact vérifié aussi par WorldLogicTest).
        Set<Integer> onlyFirst = new HashSet<>(first), onlySecond = new HashSet<>(second);
        onlyFirst.removeAll(second);
        onlySecond.removeAll(first);
        onlyFirst.removeAll(blockedSecond);
        onlySecond.removeAll(blockedFirst);
        assertTrue(onlyFirst.isEmpty() && onlySecond.isEmpty(), "même carte = mêmes gisements ; seulement 1re : "
            + describe(onlyFirst) + " ; seulement 2e : " + describe(onlySecond));
    }

    private static Set<Integer> occupiedTiles(){
        Set<Integer> tiles = new HashSet<>();
        world.tiles.eachTile(t -> {
            if(t.block() != Blocks.air) tiles.add(t.pos());
        });
        return tiles;
    }

    private static String describe(Set<Integer> tiles){
        StringBuilder b = new StringBuilder(tiles.size() + " cases");
        tiles.stream().limit(5).forEach(p -> {
            var t = world.tile(p);
            b.append(" (").append(t.x).append(',').append(t.y).append(' ').append(t.block().name).append('/').append(t.floor().name).append(')');
        });
        return b.toString();
    }

    @Test
    void oresCanBeDisabled(){
        Core.settings.put(ModSettings.ORES, false);
        Events.fire(Trigger.newGame);
        assertTrue(modOreTiles().isEmpty());
    }

    @Test
    void newGameAddsEnemyWaves(){
        int before = state.rules.spawns.size;
        Events.fire(Trigger.newGame);
        assertEquals(before + minclaude.logic.EnemyWavePlan.forDifficulty(2).size(), state.rules.spawns.size);
        assertTrue(state.rules.spawns.contains(g -> g.type == MCUnits.brute));
        var boss = state.rules.spawns.find(g -> g.type == MCUnits.warlord);
        assertSame(mindustry.content.StatusEffects.boss, boss.effect, "les boss ont l'effet « boss » du jeu");
        assertTrue(state.rules.spawns.contains(g -> g.type == MCUnits.marauder));
        assertTrue(state.rules.spawns.contains(g -> g.type == MCUnits.wasp));
        var marauder = state.rules.spawns.find(g -> g.type == MCUnits.marauder);
        assertEquals(0, marauder.getSpawned(0), "pas dès la première vague");
        assertTrue(marauder.getSpawned(marauder.begin) > 0);
    }

    @Test
    void difficultyScalesEnemyHealthAndDamage(){
        Core.settings.put(ModSettings.DIFFICULTY, 4);
        float health = state.rules.unitHealth(state.rules.waveTeam), damage = state.rules.unitDamage(state.rules.waveTeam);
        Events.fire(Trigger.newGame);
        assertEquals(health * 1.7f, state.rules.unitHealth(state.rules.waveTeam), 1e-4);
        assertEquals(damage * 1.35f, state.rules.unitDamage(state.rules.waveTeam), 1e-4);
        assertEquals(1f, state.rules.unitHealth(state.rules.defaultTeam), 1e-4, "le joueur n'est pas concerné");
    }

    @Test
    void enemyWavesCanBeDisabled(){
        Core.settings.put(ModSettings.ENEMIES, false);
        int before = state.rules.spawns.size;
        Events.fire(Trigger.newGame);
        assertEquals(before, state.rules.spawns.size);
    }

    @Test
    void smartEnemyPrefersGeneratorOverWall(){
        SmartAI.setProfile(TargetScorer.Profile.forDifficulty(3, true));
        var at = freeArea(8, 3);
        Building wall = place(Blocks.copperWall, state.rules.defaultTeam, at.x + 2, at.y + 1);
        Building generator = place(Blocks.combustionGenerator, state.rules.defaultTeam, at.x + 7, at.y + 1);
        Unit enemy = UnitTypes.dagger.spawn(state.rules.waveTeam, at.worldx(), at.y * tilesize + tilesize);

        var ai = assertInstanceOf(SmartGroundAI.class, enemy.controller());
        for(int i = 0; i < 5; i++) ai.updateUnit();
        assertSame(generator, ai.smartTarget(), "le générateur est la cible prioritaire, pas le mur plus proche");
        assertNotSame(wall, ai.smartTarget());
    }

    @Test
    void disabledProfileFallsBackToVanilla(){
        SmartAI.setProfile(TargetScorer.Profile.forDifficulty(1, true));
        var at = freeArea(6, 3);
        place(Blocks.combustionGenerator, state.rules.defaultTeam, at.x + 4, at.y + 1);
        Unit enemy = UnitTypes.dagger.spawn(state.rules.waveTeam, at.worldx(), at.y * tilesize + tilesize);
        var ai = (SmartGroundAI)enemy.controller();
        for(int i = 0; i < 5; i++) ai.updateUnit();
        assertNull(ai.smartTarget(), "difficulté Facile : comportement vanilla");
    }

    @AfterAll
    static void restore(){
        SmartAI.setProfile(null);
    }
}
