package minclaude.ai;

import arc.math.geom.Vec2;
import arc.util.Time;
import minclaude.logic.TargetScorer;
import minclaude.logic.TargetScorer.Kind;
import mindustry.ai.types.GroundAI;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.blocks.defense.turrets.Turret.TurretBuild;
import mindustry.world.blocks.power.PowerGenerator;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.BlockGroup;

import static mindustry.Vars.tilesize;

/**
 * IA terrestre ennemie à ciblage intelligent. Dans un rayon de recherche (selon la difficulté), elle choisit
 * le bâtiment le mieux noté par {@link TargetScorer} (générateurs, tourelles à sec, logistique…) et s'en approche.
 * Sinon, ou si elle reste bloquée, elle reprend le comportement vanilla (chemin vers le noyau).
 */
public class SmartGroundAI extends GroundAI{
    private static final float RESCAN_TICKS = 45f, STUCK_TICKS = 120f, GIVE_UP_TICKS = 300f;

    private Building smartTarget;
    private float rescanTimer, stuckTimer, giveUpTimer;
    private final Vec2 lastPos = new Vec2();

    @Override
    public void updateMovement(){
        // Tactiques de groupe d'abord : regroupement, flanquement et retraite passent avant la cible individuelle.
        if(followSquadOrder()) return;

        TargetScorer.Profile p = SmartAI.profile();
        if(!p.enabled() || giveUpTimer > 0f){
            giveUpTimer -= Time.delta;
            smartTarget = null;
            super.updateMovement();
            return;
        }

        rescanTimer -= Time.delta;
        if(rescanTimer <= 0f || smartTarget == null || !smartTarget.isValid()){
            rescanTimer = RESCAN_TICKS;
            smartTarget = findSmartTarget(p);
        }
        if(smartTarget == null){
            super.updateMovement();
            return;
        }

        target = smartTarget;
        float keep = unit.type.range * 0.8f;
        if(!unit.within(smartTarget, keep)){
            moveTo(smartTarget, keep);
            trackStuck();
        }
        faceTarget();
    }

    /** @return vrai si un ordre d'escouade a piloté le déplacement ce tick */
    private boolean followSquadOrder(){
        if(giveUpTimer > 0f) return false;
        var order = minclaude.MinClaudeMod.squads.order(unit);
        if(order.type() == minclaude.logic.SquadPlanner.OrderType.NONE) return false;
        float wx = order.x() * tilesize, wy = order.y() * tilesize;
        float arrive = order.type() == minclaude.logic.SquadPlanner.OrderType.GATHER ? tilesize * 2f : tilesize * 1.5f;
        if(unit.within(wx, wy, arrive)){
            // Flanc atteint : on reprend l'assaut normal. Regroupement / retraite : on attend sur place.
            if(order.type() == minclaude.logic.SquadPlanner.OrderType.FLANK) return false;
            faceTarget();
            return true;
        }
        moveTo(new arc.math.geom.Vec2(wx, wy), arrive * 0.8f);
        trackStuck();
        faceTarget();
        return true;
    }

    /** Un déplacement en ligne droite peut buter sur un mur : on abandonne la cible quelque temps. */
    private void trackStuck(){
        if(unit.within(lastPos, tilesize * 0.5f)){
            stuckTimer += Time.delta;
            if(stuckTimer >= STUCK_TICKS){
                stuckTimer = 0f;
                smartTarget = null;
                giveUpTimer = GIVE_UP_TICKS;
            }
        }else{
            stuckTimer = 0f;
            lastPos.set(unit.x, unit.y);
        }
    }

    /** Cible choisie par le ciblage intelligent, ou null (comportement vanilla). */
    public Building smartTarget(){
        return smartTarget;
    }

    @Override
    public Teamc findMainTarget(float x, float y, float range, boolean air, boolean ground){
        if(smartTarget != null && smartTarget.isValid() && unit.within(smartTarget, range + smartTarget.hitSize() / 2f)){
            return smartTarget;
        }
        return super.findMainTarget(x, y, range, air, ground);
    }

    private Building findSmartTarget(TargetScorer.Profile p){
        Building[] best = {null};
        float[] bestScore = {Float.NEGATIVE_INFINITY};
        float radius = p.seekTiles() * tilesize;
        float range = unit.type.range;
        Units.nearbyBuildings(unit.x, unit.y, radius, b -> {
            if(b.team == unit.team || b.team == Team.derelict || !b.block.targetable) return;
            float dst = unit.dst(b);
            float tiles = dst / tilesize;
            if(!TargetScorer.inSeekRange(p, tiles)) return;
            float s = TargetScorer.score(p, kind(b), tiles, b.healthf(), dst <= range);
            if(s > bestScore[0]){
                bestScore[0] = s;
                best[0] = b;
            }
        });
        return best[0];
    }

    static Kind kind(Building b){
        if(b instanceof CoreBlock.CoreBuild) return Kind.CORE;
        if(b instanceof TurretBuild t) return t.hasAmmo() ? Kind.TURRET : Kind.TURRET_NO_AMMO;
        if(b.block instanceof PowerGenerator) return Kind.GENERATOR;
        if(b.block instanceof PowerNode) return Kind.POWER_NODE;
        if(b.block.group == BlockGroup.transportation) return Kind.CONVEYOR;
        if(b.block instanceof GenericCrafter) return Kind.CRAFTER;
        if(b.block instanceof Wall) return Kind.WALL;
        return Kind.OTHER;
    }
}
