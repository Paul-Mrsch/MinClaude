package minclaude.ai;

import arc.Events;
import arc.util.Time;
import minclaude.ModSettings;
import minclaude.logic.SquadPlanner;
import minclaude.logic.SquadPlanner.*;
import mindustry.game.EventType.*;
import mindustry.gen.Unit;

import java.util.*;

import static mindustry.Vars.*;

/**
 * Tactiques de groupe des vagues : une fois par seconde de jeu, regroupe les unités terrestres ennemies
 * pilotées par {@link SmartGroundAI} et calcule leurs ordres (regroupement, flanquement, retraite).
 */
public final class SquadManager{
    private final SquadPlanner planner = new SquadPlanner();
    private volatile Map<Integer, Order> orders = Map.of();
    private float timer;
    private Tactics forced;

    public void register(){
        Events.run(Trigger.update, this::update);
        Events.on(WorldLoadEvent.class, e -> reset());
        Events.on(ResetEvent.class, e -> reset());
    }

    public Order order(Unit unit){
        return orders.getOrDefault(unit.id, Order.NONE);
    }

    /** Force les paramètres (tests) ; {@code null} revient aux options. */
    public void setTactics(Tactics t){
        forced = t;
    }

    public Tactics tactics(){
        return forced != null ? forced : Tactics.forDifficulty(ModSettings.difficulty(), ModSettings.smartAi());
    }

    private void reset(){
        orders = Map.of();
        timer = 0f;
    }

    private void update(){
        if(!state.isPlaying()) return;
        timer += Time.delta;
        if(timer < 60f) return;
        timer = 0f;
        replan();
    }

    /** Recalcule les ordres immédiatement. */
    public void replan(){
        Tactics t = tactics();
        var core = state.rules.defaultTeam.core();
        if(!t.enabled() || core == null){
            orders = Map.of();
            return;
        }
        List<Member> members = new ArrayList<>();
        // Groups.unit plutôt que team.data().units, qui n'est mis à jour qu'à la frame suivante.
        mindustry.gen.Groups.unit.each(u -> {
            if(u.team == state.rules.waveTeam && u.controller() instanceof SmartGroundAI){
                members.add(new Member(u.id, u.x / tilesize, u.y / tilesize, u.healthf()));
            }
        });
        orders = new HashMap<>(planner.plan(members, core.x / tilesize, core.y / tilesize, t, (float)(state.tick / 60f)));
    }
}
