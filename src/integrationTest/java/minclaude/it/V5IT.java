package minclaude.it;

import arc.util.Time;
import minclaude.content.*;
import mindustry.content.*;
import mindustry.core.GameState.State;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.world.blocks.power.ConsumeGenerator.ConsumeGeneratorBuild;
import mindustry.world.blocks.power.PowerNode;
import org.junit.jupiter.api.*;

import static minclaude.it.HeadlessGame.*;
import static mindustry.Vars.*;
import static org.junit.jupiter.api.Assertions.*;

/** V5 dans le vrai jeu : les bâtiments d'énergie et de soutien fonctionnent comme annoncé. */
class V5IT{
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
        state.rules.waves = false;
    }

    @Test
    void batteriesStoreTheirCapacity(){
        var at = freeArea(6, 3);
        Building invar = place(MCBlocks.invarBattery, state.rules.defaultTeam, at.x, at.y);
        Building quantum = place(MCBlocks.quantumCapacitor, state.rules.defaultTeam, at.x + 4, at.y + 1);
        assertEquals(36000f, invar.power.graph.getTotalBatteryCapacity(), 1f);
        assertEquals(250000f, quantum.power.graph.getTotalBatteryCapacity(), 1f);
    }

    @Test
    void brineGeneratorRunsOnBrineOnly(){
        var at = freeArea(2, 2);
        var gen = (ConsumeGeneratorBuild)place(MCBlocks.brineGenerator, state.rules.defaultTeam, at.x, at.y);
        run(60);
        assertEquals(0f, gen.productionEfficiency, "sans saumure : rien");
        for(int i = 0; i < 120; i++){
            gen.liquids.add(MCLiquids.brine, Math.max(0f, 20f - gen.liquids.get(MCLiquids.brine)));
            run(1);
        }
        assertEquals(1f, gen.productionEfficiency, 0.01f, "avec saumure : pleine production");
        assertEquals(300f, gen.getPowerProduction() * gen.productionEfficiency * 60f, 1f);
    }

    @Test
    void industrialTurbineNeedsFuelAndWater(){
        var at = freeArea(3, 3);
        var gen = (ConsumeGeneratorBuild)place(MCBlocks.industrialTurbine, state.rules.defaultTeam, at.x + 1, at.y + 1);
        gen.items.add(Items.coal, 10);
        run(60);
        assertEquals(0f, gen.productionEfficiency, "sans eau : rien");
        int coalBefore = gen.items.get(Items.coal);
        for(int i = 0; i < 300; i++){
            gen.liquids.add(Liquids.water, Math.max(0f, 40f - gen.liquids.get(Liquids.water)));
            run(1);
        }
        assertEquals(1f, gen.productionEfficiency, 0.01f, "charbon + eau : pleine production");
        int burnt = coalBefore - gen.items.get(Items.coal);
        assertTrue(burnt >= 3 && burnt <= 5, "un combustible toutes les 1,5 s, brûlés en 5 s : " + burnt);
    }

    @Test
    void longRangeNodeReachesFurtherThanTheLargeNode(){
        var core = state.rules.defaultTeam.core().tile;
        var node = (PowerNode)MCBlocks.longRangeNode;
        Building a = null, near = null, far = null;
        // Trois emplacements libres alignés : origine, +26 cases (portée 28), +34 cases (hors portée).
        for(int y = 2; y < world.height() - 2 && far == null; y++){
            for(int x = 2; x + 36 < world.width() && far == null; x++){
                if(Math.abs(y - core.y) < 6) continue;
                if(free(x, y) && free(x + 26, y) && free(x + 34, y)){
                    a = place(MCBlocks.longRangeNode, state.rules.defaultTeam, x, y);
                    near = place(MCBlocks.longRangeNode, state.rules.defaultTeam, x + 26, y);
                    far = place(MCBlocks.longRangeNode, state.rules.defaultTeam, x + 34, y);
                }
            }
        }
        assertNotNull(far, "trois emplacements libres pour les nœuds");
        assertTrue(node.linkValid(a, near), "à 26 cases : relié");
        assertFalse(node.linkValid(a, far), "à 34 cases : hors portée");
        assertTrue(26 > ((PowerNode)Blocks.powerNodeLarge).laserRange, "le grand nœud vanilla n'y arrive pas");
    }

    private static boolean free(int x, int y){
        for(int i = -1; i <= 2; i++){
            for(int j = -1; j <= 2; j++){
                var t = world.tile(x + i, y + j);
                if(t == null || t.block() != Blocks.air || t.floor().isLiquid || t.floor().solid) return false;
            }
        }
        return true;
    }

    @Test
    void tempestShootsGroundAndAir(){
        // Sans la règle de triche, qui donnerait des munitions infinies : la Tempête n'a pas besoin d'énergie.
        // Cibles dans la zone libre, à 12 cases : une unité née dans un mur mourrait sans qu'on tire.
        var at = freeArea(3, 15);
        Building tempest = place(MCBlocks.tempest, state.rules.defaultTeam, at.x + 1, at.y + 1);
        Unit ground = UnitTypes.dagger.spawn(state.rules.waveTeam, tempest.x, tempest.y + 12 * tilesize);
        Unit air = UnitTypes.flare.spawn(state.rules.waveTeam, tempest.x + tilesize, tempest.y + 12 * tilesize);
        for(Unit u : new Unit[]{ground, air}){
            u.apply(StatusEffects.unmoving, 100000f);
            u.apply(StatusEffects.disarmed, 100000f);
        }
        run(60);
        assertFalse(ground.dead() || air.dead(), "sans munitions, la Tempête ne tire pas");
        tempest.handleStack(MCItems.steel, 40, null);
        for(int i = 0; i < 900 && (!ground.dead() || !air.dead()); i++) run(1);
        assertTrue(ground.dead(), "unité au sol détruite");
        assertTrue(air.dead(), "unité volante détruite");
    }

    @Test
    void restorationDomeRepairsAWideArea(){
        // Alimenté par une batterie en invar chargée, posée contre le dôme ; sans cristal quantique (pas de bonus).
        var at = freeArea(5, 3);
        place(MCBlocks.restorationDome, state.rules.defaultTeam, at.x + 1, at.y + 1);
        Building battery = place(MCBlocks.invarBattery, state.rules.defaultTeam, at.x + 3, at.y);
        battery.power.status = 1f;
        // Un mur à 15 cases : hors de portée du projecteur de réparation vanilla (10,6 cases), dans celle du dôme (18,75).
        Building wall = null;
        for(int r = 15; r < 18 && wall == null; r++){
            for(int[] d : new int[][]{{r, 0}, {-r, 0}, {0, r}, {0, -r}}){
                var t = world.tile(at.x + 1 + d[0], at.y + 1 + d[1]);
                if(t != null && t.block() == Blocks.air && !t.floor().solid && !t.floor().isLiquid){
                    wall = place(Blocks.titaniumWall, state.rules.defaultTeam, t.x, t.y);
                    break;
                }
            }
        }
        assertNotNull(wall, "emplacement pour le mur");
        wall.health = wall.maxHealth * 0.2f;
        run(900);
        assertTrue(battery.power.graph.getBatteryStored() < 36000f, "le dôme consomme l'énergie de la batterie");
        // 10 % toutes les 3,3 s : 4 réparations en 15 s.
        assertEquals(0.6f, wall.health / wall.maxHealth, 0.11f, "réparé de 40 % en 15 s");
    }
}
