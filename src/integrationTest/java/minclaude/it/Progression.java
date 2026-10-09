package minclaude.it;

import mindustry.content.*;
import mindustry.content.TechTree.TechNode;
import mindustry.ctype.UnlockableContent;
import mindustry.type.*;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.production.*;
import mindustry.world.consumers.*;

import java.util.*;

import static mindustry.Vars.content;

/**
 * Simulation de progression sur Serpulo, de Ground Zero à la fin de jeu, à partir des vraies recettes du jeu.
 *
 * <p>L'<b>étape</b> d'un contenu compte les transformations nécessaires pour l'obtenir depuis le départ de
 * Ground Zero (cuivre, plomb, sable, ferraille, charbon, eau) : un bloc est disponible à l'étape de son matériau
 * le plus avancé ; un objet produit par une usine arrive une étape après l'usine et ses entrées ; un minerai
 * arrive à l'étape de la première foreuse assez puissante. Seuls les blocs de l'arbre de Serpulo comptent.
 */
final class Progression{
    static final int UNREACHABLE = Integer.MAX_VALUE / 4;

    private final Map<UnlockableContent, Integer> stage = new HashMap<>();
    private final List<Block> blocks = new ArrayList<>();

    Progression(){
        for(Block b : content.blocks()){
            if(inSerpuloTree(b)) blocks.add(b);
        }
        // Départ de Ground Zero : minerais de surface et eau, minables sans recherche.
        for(Item i : new Item[]{Items.copper, Items.lead, Items.sand, Items.scrap}) stage.put(i, 0);
        boolean changed = true;
        while(changed){
            changed = false;
            for(Block b : blocks){
                int built = cost(b);
                if(built >= UNREACHABLE) continue;
                if(b instanceof Pump) changed |= lower(Liquids.water, built);
                if(b instanceof Drill d){
                    for(Item ore : minable()) if(ore.hardness <= d.tier) changed |= lower(ore, built);
                }
                int run = usable(b);
                if(run >= UNREACHABLE) continue;
                for(UnlockableContent out : outputs(b)) changed |= lower(out, run + 1);
            }
        }
    }

    /** Étape d'un objet ou d'un liquide (UNREACHABLE s'il n'est pas obtenable). */
    int of(UnlockableContent c){
        return stage.getOrDefault(c, UNREACHABLE);
    }

    /** Étape où le bloc peut être construit. */
    int cost(Block b){
        int s = 0;
        for(ItemStack st : b.requirements) s = Math.max(s, of(st.item));
        return s;
    }

    /** Étape où le bloc peut être construit et alimenté (entrées obligatoires ; l'énergie n'est pas comptée). */
    int usable(Block b){
        int s = cost(b);
        for(Consume c : b.nonOptionalConsumers) s = Math.max(s, input(c));
        return s;
    }

    List<Block> blocks(){
        return blocks;
    }

    private int input(Consume c){
        int s = 0;
        // Les objets explosifs ne sont pas une entrée : ce consommateur ne fait qu'endommager le générateur.
        if(c instanceof ConsumeItemExplode) return 0;
        if(c instanceof ConsumeItems ci) for(ItemStack st : ci.items) s = Math.max(s, of(st.item));
        else if(c instanceof ConsumeItemFilter f){
            int best = UNREACHABLE;
            for(Item i : content.items()) if(f.filter.get(i)) best = Math.min(best, of(i));
            s = best;
        }else if(c instanceof ConsumeLiquid cl) s = of(cl.liquid);
        else if(c instanceof ConsumeLiquids cl) for(LiquidStack st : cl.liquids) s = Math.max(s, of(st.liquid));
        return s;
    }

    private static List<UnlockableContent> outputs(Block b){
        List<UnlockableContent> out = new ArrayList<>();
        if(b instanceof GenericCrafter gc){
            if(gc.outputItems != null) for(ItemStack s : gc.outputItems) out.add(s.item);
            if(gc.outputLiquids != null) for(LiquidStack s : gc.outputLiquids) out.add(s.liquid);
        }else if(b instanceof Separator sep){
            for(ItemStack s : sep.results) out.add(s.item);
        }
        return out;
    }

    private Set<Item> minable(){
        Set<Item> ores = new HashSet<>();
        for(Block b : content.blocks()){
            if(b instanceof Floor f && f.itemDrop != null && !f.wallOre && serpuloItem(f.itemDrop)) ores.add(f.itemDrop);
        }
        return ores;
    }

    private static boolean serpuloItem(Item i){
        return Items.serpuloItems.contains(i) || minclaude.content.MCItems.all.contains(i);
    }

    private boolean lower(UnlockableContent c, int s){
        if(s < of(c)){
            stage.put(c, s);
            return true;
        }
        return false;
    }

    static boolean inSerpuloTree(UnlockableContent c){
        TechNode n = c.techNode;
        if(n == null) return false;
        while(n.parent != null) n = n.parent;
        return n.content == Blocks.coreShard;
    }
}
