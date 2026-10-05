package minclaude.ai;

import arc.Events;
import minclaude.MinClaudeMod;
import minclaude.ModSettings;
import minclaude.content.MCUnits;
import minclaude.logic.AdaptiveWaves;
import minclaude.logic.AdaptiveWaves.*;
import mindustry.game.EventType.*;
import mindustry.game.SpawnGroup;
import mindustry.type.UnitType;

import static mindustry.Vars.*;

/**
 * Applique l'adaptation des vagues après chaque vague : les groupes des ennemis du mod reçoivent un bonus
 * d'unités selon leur famille et la défense actuelle du joueur. Le bonus appliqué est mémorisé dans les règles
 * (étiquettes enregistrées dans la sauvegarde) pour pouvoir être retiré ou modifié sans perdre la valeur de base.
 */
public final class WaveAdapter{
    static final String TAG = "minclaude-adapt-";

    private volatile Adaptation current = Adaptation.NONE;

    public void register(){
        Events.on(WaveEvent.class, e -> adapt());
        Events.on(WorldLoadEvent.class, e -> current = Adaptation.NONE);
    }

    public Adaptation current(){
        return current;
    }

    public static Family family(UnitType type){
        if(MCUnits.airEnemies.contains(type)) return Family.AIR;
        if(MCUnits.siegeEnemies.contains(type)) return Family.SIEGE;
        if(MCUnits.armorEnemies.contains(type)) return Family.ARMOR;
        if(MCUnits.swarmEnemies.contains(type)) return Family.SWARM;
        return null;
    }

    /** Recalcule et applique l'adaptation pour la prochaine vague. */
    public void adapt(){
        if(net.client() || !state.rules.waves) return;
        int difficulty = ModSettings.smartAi() ? ModSettings.difficulty() : 1;
        MinClaudeMod.tracker.scanNow();
        Adaptation a = AdaptiveWaves.compute(MinClaudeMod.tracker.base().defense().profile(), difficulty);
        for(int i = 0; i < state.rules.spawns.size; i++){
            SpawnGroup g = state.rules.spawns.get(i);
            Family f = family(g.type);
            if(f == null) continue;
            String key = TAG + i;
            int old = state.rules.tags.getInt(key, 0), now = a.bonus(f);
            g.unitAmount = Math.max(1, g.unitAmount - old + now);
            state.rules.tags.put(key, String.valueOf(now));
        }
        current = a;
    }
}
