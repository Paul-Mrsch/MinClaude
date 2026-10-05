package minclaude.it;

import arc.util.Time;
import minclaude.MinClaudeMod;
import minclaude.ai.SmartAI;
import minclaude.logic.SquadPlanner.*;
import minclaude.logic.TargetScorer;
import mindustry.content.UnitTypes;
import mindustry.core.GameState.State;
import mindustry.gen.Unit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static minclaude.it.HeadlessGame.*;
import static mindustry.Vars.*;
import static org.junit.jupiter.api.Assertions.*;

/** Tactiques de groupe des vagues dans le vrai jeu : ordres calculés pour de vraies unités et appliqués par l'IA. */
class SquadIT{
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
        MinClaudeMod.squads.setTactics(Tactics.forDifficulty(3, true));
        SmartAI.setProfile(TargetScorer.Profile.forDifficulty(3, true));
    }

    @AfterEach
    void restore(){
        MinClaudeMod.squads.setTactics(null);
        SmartAI.setProfile(null);
    }

    /** Unités ennemies alignées perpendiculairement à la direction du noyau, loin de celui-ci. */
    private static List<Unit> spawnLine(int n){
        var core = state.rules.defaultTeam.core();
        var at = freeArea(2, n + 2);
        List<Unit> units = new ArrayList<>();
        for(int i = 0; i < n; i++){
            units.add(UnitTypes.dagger.spawn(state.rules.waveTeam, at.worldx(), at.worldy() + i * tilesize));
        }
        assertTrue(core.dst(units.get(0)) > 3 * tilesize);
        return units;
    }

    @Test
    void fullSquadGetsFlankOrders(){
        List<Unit> units = spawnLine(6);
        MinClaudeMod.squads.replan();
        long flank = units.stream().filter(u -> MinClaudeMod.squads.order(u).type() == OrderType.FLANK).count();
        assertTrue(flank >= 2, "des ailes sont désignées : " + flank);
        assertTrue(units.stream().anyMatch(u -> MinClaudeMod.squads.order(u).type() == OrderType.NONE), "le centre attaque normalement");
    }

    @Test
    void smallSquadGathersAndAiMovesTowardRallyPoint(){
        List<Unit> units = spawnLine(2);
        Unit u = units.get(1);
        MinClaudeMod.squads.replan();
        Order o = MinClaudeMod.squads.order(u);
        assertEquals(OrderType.GATHER, o.type());
        float before = u.dst(o.x() * tilesize, o.y() * tilesize);
        for(int i = 0; i < 30; i++){
            u.controller().updateUnit();
            u.update();
        }
        assertTrue(u.dst(o.x() * tilesize, o.y() * tilesize) <= before, "l'unité ne s'éloigne pas du point de ralliement");
    }

    @Test
    void damagedUnitRetreats(){
        List<Unit> units = spawnLine(5);
        units.get(2).health = units.get(2).maxHealth * 0.1f;
        MinClaudeMod.squads.replan();
        assertEquals(OrderType.RETREAT, MinClaudeMod.squads.order(units.get(2)).type());
    }

    @Test
    void noTacticsOnEasy(){
        MinClaudeMod.squads.setTactics(Tactics.forDifficulty(1, true));
        List<Unit> units = spawnLine(6);
        MinClaudeMod.squads.replan();
        assertTrue(units.stream().allMatch(u -> MinClaudeMod.squads.order(u).type() == OrderType.NONE));
    }
}
