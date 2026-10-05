package minclaude.ai;

import minclaude.logic.TargetScorer;
import mindustry.ai.types.FlyingAI;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.*;

import static mindustry.Vars.tilesize;

/**
 * IA volante ennemie à ciblage intelligent : même notation que l'IA terrestre ({@link TargetScorer}), dans un rayon
 * 1,5 fois plus grand (les volants passent au-dessus des murs). Les {@code targetFlags} propres à l'unité
 * (ex. la guêpe vise les générateurs) restent prioritaires, comme en vanilla.
 */
public class SmartFlyingAI extends FlyingAI{
    @Override
    public Teamc findMainTarget(float x, float y, float range, boolean air, boolean ground){
        Teamc vanilla = super.findMainTarget(x, y, range, air, ground);
        TargetScorer.Profile p = SmartAI.profile();
        if(!p.enabled() || !ground) return vanilla;
        // Une cible désignée par les drapeaux de l'unité (générateurs, usines…) est conservée.
        if(vanilla instanceof Building && unit.type.targetFlags.length > 0 && unit.type.targetFlags[0] != null) return vanilla;

        Building[] best = {null};
        float[] bestScore = {Float.NEGATIVE_INFINITY};
        float radius = p.seekTiles() * 1.5f * tilesize;
        Units.nearbyBuildings(x, y, radius, b -> {
            if(b.team == unit.team || b.team == Team.derelict || !b.block.targetable) return;
            float dst = b.dst(x, y);
            float s = TargetScorer.score(p, SmartGroundAI.kind(b), dst / tilesize / 1.5f, b.healthf(), dst <= range);
            if(s > bestScore[0]){
                bestScore[0] = s;
                best[0] = b;
            }
        });
        return best[0] != null ? best[0] : vanilla;
    }
}
