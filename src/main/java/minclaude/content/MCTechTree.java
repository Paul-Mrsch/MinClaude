package minclaude.content;

import mindustry.content.*;
import mindustry.ctype.UnlockableContent;
import mindustry.type.ItemStack;

import static mindustry.content.TechTree.TechNode;

/** Branche le contenu du mod dans l'arbre technologique de Serpulo, sous des nœuds vanilla existants. */
public final class MCTechTree{
    private MCTechTree(){}

    public static void load(){
        TechNode cobalt = node(Items.lead.techNode, MCItems.cobalt, ItemStack.empty);
        node(Blocks.siliconSmelter.techNode, MCBlocks.cobaltSmelter, MCBlocks.cobaltSmelter.researchRequirements());
        TechNode wall = node(Blocks.titaniumWall.techNode, MCBlocks.cobaltWall, MCBlocks.cobaltWall.researchRequirements());
        node(wall, MCBlocks.cobaltWallLarge, MCBlocks.cobaltWallLarge.researchRequirements());
    }

    private static TechNode node(TechNode parent, UnlockableContent content, ItemStack[] requirements){
        if(parent == null) throw new IllegalStateException("Nœud parent introuvable pour " + content.name);
        return new TechNode(parent, content, requirements);
    }
}
