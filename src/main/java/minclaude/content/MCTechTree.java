package minclaude.content;

import mindustry.content.*;
import mindustry.ctype.UnlockableContent;
import mindustry.type.ItemStack;

import static mindustry.content.TechTree.TechNode;

/** Branche le contenu du mod dans l'arbre technologique de Serpulo, sous des nœuds vanilla existants. */
public final class MCTechTree{
    private MCTechTree(){}

    public static void load(){
        // Ressources : obtenues en minant ou en produisant, sans coût de recherche.
        node(Items.copper.techNode, MCItems.nickel, ItemStack.empty);
        node(Items.copper.techNode, MCItems.zinc, ItemStack.empty);
        TechNode bauxite = node(Items.lead.techNode, MCItems.bauxite, ItemStack.empty);
        node(Items.lead.techNode, MCItems.cobalt, ItemStack.empty);
        node(bauxite, MCItems.aluminum, ItemStack.empty);
        node(MCItems.zinc.techNode, MCItems.brass, ItemStack.empty);

        // Industries
        node(Blocks.siliconSmelter.techNode, MCBlocks.cobaltSmelter);
        node(Blocks.siliconSmelter.techNode, MCBlocks.aluminumSmelter);
        node(Blocks.graphitePress.techNode, MCBlocks.brassFoundry);
        node(Blocks.separator.techNode, MCBlocks.oreWasher);
        node(Blocks.pneumaticDrill.techNode, MCBlocks.percussionDrill);

        // Défense
        TechNode wall = node(Blocks.titaniumWall.techNode, MCBlocks.cobaltWall);
        node(wall, MCBlocks.cobaltWallLarge);
        TechNode nickelWall = node(Blocks.copperWall.techNode, MCBlocks.nickelWall);
        node(nickelWall, MCBlocks.nickelWallLarge);
        node(Blocks.duo.techNode, MCBlocks.rivet);

        // Logistique et énergie
        node(Blocks.titaniumConveyor.techNode, MCBlocks.reinforcedConveyor);
        node(Blocks.powerNode.techNode, MCBlocks.aluminumNode);

        // Unités alliées (les ennemies ne sont pas recherchables)
        node(UnitTypes.dagger.techNode, MCUnits.warden);
        node(UnitTypes.flare.techNode, MCUnits.aid);
    }

    private static TechNode node(TechNode parent, UnlockableContent content){
        return node(parent, content, content.researchRequirements());
    }

    private static TechNode node(TechNode parent, UnlockableContent content, ItemStack[] requirements){
        if(parent == null) throw new IllegalStateException("Nœud parent introuvable pour " + content.name);
        return new TechNode(parent, content, requirements);
    }
}
