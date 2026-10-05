package minclaude.tracking;

import minclaude.stats.FlowCounter;
import mindustry.gen.Building;
import mindustry.gen.Teamc;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.Vars.content;

/**
 * Mesure exacte des flux : remplace le type de bâtiment des noyaux vanilla par {@link TrackedCoreBuild}, qui compte
 * chaque objet accepté ou détruit (noyau plein). Les noyaux d'autres mods, qui ont leur propre classe, ne sont pas
 * touchés : ils restent suivis par l'estimation.
 */
public final class CoreFlowHook{
    /** Compteurs de l'équipe suivie (une seule à la fois : celle du joueur). */
    public static final FlowCounter counter = new FlowCounter();
    static volatile mindustry.game.Team trackedTeam;

    private CoreFlowHook(){}

    /** @return nombre de types de noyau instrumentés */
    public static int install(){
        int n = 0;
        for(Block b : content.blocks()){
            if(b instanceof CoreBlock core && core.buildType.get().getClass() == CoreBlock.CoreBuild.class){
                core.buildType = () -> new TrackedCoreBuild(core);
                n++;
            }
        }
        return n;
    }

    /** Noyau instrumenté : compte les entrées acceptées et les objets détruits. */
    public static class TrackedCoreBuild extends CoreBlock.CoreBuild{
        public TrackedCoreBuild(CoreBlock block){
            block.super();
        }

        @Override
        public void handleItem(Building source, Item item){
            int before = items.get(item);
            super.handleItem(source, item);
            count(item, items.get(item) - before, 1);
        }

        @Override
        public void handleStack(Item item, int amount, Teamc source){
            int before = items.get(item);
            super.handleStack(item, amount, source);
            count(item, items.get(item) - before, amount);
        }

        private void count(Item item, int accepted, int offered){
            if(team != trackedTeam) return;
            counter.record(item.id, accepted, offered - Math.max(0, accepted));
        }
    }
}
