package minclaude.ai;

import arc.util.Time;
import minclaude.ModSettings;
import minclaude.logic.TargetScorer;
import mindustry.ai.types.GroundAI;
import mindustry.type.UnitType;

import static mindustry.Vars.content;

/**
 * Installe l'IA ennemie intelligente : toute unité terrestre dont l'IA par défaut est le GroundAI vanilla
 * (unités des vagues) utilise {@link SmartGroundAI}, qui se comporte comme le vanilla quand l'option est désactivée.
 * Les unités du joueur ne sont pas concernées : elles sont pilotées par CommandAI.
 */
public final class SmartAI{
    private static TargetScorer.Profile profile = TargetScorer.Profile.forDifficulty(2, true);
    private static float lastRefresh = -1000f;
    private static boolean forced;

    private SmartAI(){}

    /** @return nombre de types d'unités modifiés */
    public static int install(){
        int n = 0;
        for(UnitType type : content.units()){
            if(type.flying || type.naval) continue;
            if(type.aiController.get().getClass() != GroundAI.class) continue;
            type.aiController = SmartGroundAI::new;
            n++;
        }
        return n;
    }

    /** Profil courant, relu dans les options au plus une fois par seconde. */
    public static TargetScorer.Profile profile(){
        if(forced) return profile;
        if(Time.time - lastRefresh > 60f || Time.time < lastRefresh){
            lastRefresh = Time.time;
            profile = TargetScorer.Profile.forDifficulty(ModSettings.difficulty(), ModSettings.smartAi());
        }
        return profile;
    }

    /** Force le profil (tests) ; {@code null} revient aux options. */
    public static void setProfile(TargetScorer.Profile p){
        forced = p != null;
        if(p != null) profile = p;
        else lastRefresh = -1000f;
    }
}
