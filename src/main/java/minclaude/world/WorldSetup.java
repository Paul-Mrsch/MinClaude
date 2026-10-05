package minclaude.world;

import arc.Events;
import arc.util.Log;
import minclaude.ModSettings;
import minclaude.content.MCBlocks;
import minclaude.logic.EnemyWavePlan;
import minclaude.logic.OreScatter;
import mindustry.content.Blocks;
import mindustry.game.EventType.Trigger;
import mindustry.game.SpawnGroup;
import mindustry.type.UnitType;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.OreBlock;

import static mindustry.Vars.*;

/**
 * Préparation d'une nouvelle partie (carte ou secteur) : gisements des minerais du mod et vagues des ennemis du mod.
 * Appliquée une seule fois par partie : un marqueur dans les règles (enregistrées dans la sauvegarde) l'empêche de recommencer.
 */
public final class WorldSetup{
    public static final String MARKER = "minclaude-setup";

    private WorldSetup(){}

    public static void register(){
        Events.run(Trigger.newGame, WorldSetup::onNewGame);
    }

    public static void onNewGame(){
        if(net.client() || state.rules.editor) return;
        if(state.rules.tags.getBool(MARKER)) return;
        state.rules.tags.put(MARKER, "true");

        if(ModSettings.ores()){
            int placed = placeOres(seed());
            Log.info("[MinClaude] @ cases de minerai ajoutées.", placed);
        }
        if(state.rules.waves && ModSettings.enemies()){
            addEnemyWaves(ModSettings.difficulty());
        }
    }

    /** Graine stable : même carte ou même secteur = mêmes gisements. */
    static long seed(){
        if(state.rules.sector != null) return state.rules.sector.planet.id * 100_003L + state.rules.sector.id;
        return state.map == null ? 0 : state.map.name().hashCode();
    }

    public static int placeOres(long seed){
        OreBlock[] ores = MCBlocks.ores.toArray(OreBlock.class);
        OreScatter.OreSpec[] specs = new OreScatter.OreSpec[ores.length];
        for(int i = 0; i < ores.length; i++) specs[i] = new OreScatter.OreSpec(ores[i].name, ores[i].oreThreshold, ores[i].oreScale / 2f);

        int placed = OreScatter.scatter(world.width(), world.height(), seed, specs, WorldSetup::eligible,
            (x, y, i) -> world.tile(x, y).setOverlay(ores[i]));

        if(!headless && placed > 0){
            renderer.blocks.floor.reload();
            renderer.minimap.updateAll();
        }
        return placed;
    }

    /** Sol nu et praticable : pas de bâtiment, pas de minerai existant, pas de liquide. */
    static boolean eligible(int x, int y){
        Tile t = world.tile(x, y);
        return t != null && t.block() == Blocks.air && t.overlay() == Blocks.air && !t.floor().isLiquid && !t.floor().solid
            && t.floor() != Blocks.space && t.floor() != Blocks.empty;
    }

    public static void addEnemyWaves(int difficulty){
        for(EnemyWavePlan.Group g : EnemyWavePlan.forDifficulty(difficulty)){
            UnitType type = content.unit("minclaude-" + g.unit());
            if(type == null) type = content.unit(g.unit()); // tests headless : pas de préfixe
            if(type == null) continue;
            SpawnGroup group = new SpawnGroup(type);
            group.begin = g.begin();
            group.spacing = g.spacing();
            group.unitAmount = g.amount();
            group.unitScaling = g.scaling();
            group.max = g.max();
            state.rules.spawns.add(group);
        }
    }
}
