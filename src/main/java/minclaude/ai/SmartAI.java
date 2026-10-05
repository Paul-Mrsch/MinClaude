package minclaude.ai;

import arc.util.Time;
import minclaude.ModSettings;
import minclaude.logic.TargetScorer;
import mindustry.ai.types.FlyingAI;
import mindustry.ai.types.GroundAI;
import mindustry.type.UnitType;

import static mindustry.Vars.content;

/**
 * Installe l'IA ennemie intelligente : toute unité dont l'IA par défaut est le GroundAI ou le FlyingAI vanilla
 * (unités des vagues) utilise {@link SmartGroundAI} ou {@link SmartFlyingAI}, qui se comportent comme le vanilla
 * quand l'option est désactivée.
 * Les unités du joueur ne sont pas concernées : elles sont pilotées par CommandAI.
 */
public final class SmartAI{
    private static TargetScorer.Profile profile = TargetScorer.Profile.forDifficulty(2, true);
    private static float lastRefresh = -1000f;
    private static boolean forced;

    private SmartAI(){}

    /** @return nombre de types d'unités modifiés (terrestres + volants) */
    public static int install(){
        int n = 0;
        for(UnitType type : content.units()){
            // Navals, mineurs, constructeurs et unités sans arme gardent leur IA.
            if(type.naval || !type.hasWeapons() || type.mineTier > 0 || type.buildSpeed > 0) continue;
            Class<?> ai = type.aiController.get().getClass();
            if(!type.flying && ai == GroundAI.class){
                type.aiController = SmartGroundAI::new;
                n++;
            }else if(type.flying && ai == FlyingAI.class){
                type.aiController = SmartFlyingAI::new;
                n++;
            }
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
