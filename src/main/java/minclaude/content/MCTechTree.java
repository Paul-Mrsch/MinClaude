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
        node(Items.titanium.techNode, MCItems.chrome, ItemStack.empty);
        TechNode steel = node(Items.scrap.techNode, MCItems.steel, ItemStack.empty);
        node(steel, MCItems.invar, ItemStack.empty);
        node(Liquids.water.techNode, MCLiquids.brine, ItemStack.empty);
        node(Liquids.water.techNode, MCLiquids.nitrogen, ItemStack.empty);

        // Industries
        node(Blocks.siliconSmelter.techNode, MCBlocks.cobaltSmelter);
        node(Blocks.siliconSmelter.techNode, MCBlocks.aluminumSmelter);
        node(Blocks.graphitePress.techNode, MCBlocks.brassFoundry);
        node(Blocks.separator.techNode, MCBlocks.oreWasher);
        node(Blocks.pneumaticDrill.techNode, MCBlocks.percussionDrill);
        TechNode furnace = node(MCBlocks.aluminumSmelter.techNode, MCBlocks.steelFurnace);
        node(furnace, MCBlocks.alloyPress);
        TechNode mixer = node(MCBlocks.oreWasher.techNode, MCBlocks.brineMixer);
        node(mixer, MCBlocks.electrolyzer);
        node(MCBlocks.aluminumSmelter.techNode, MCBlocks.cryogenizer);

        // Défense
        TechNode wall = node(Blocks.titaniumWall.techNode, MCBlocks.cobaltWall);
        node(wall, MCBlocks.cobaltWallLarge);
        TechNode nickelWall = node(Blocks.copperWall.techNode, MCBlocks.nickelWall);
        node(nickelWall, MCBlocks.nickelWallLarge);
        TechNode rivet = node(Blocks.duo.techNode, MCBlocks.rivet);
        node(rivet, MCBlocks.volley);
        node(Blocks.wave.techNode, MCBlocks.frost);
        TechNode steelWall = node(MCBlocks.cobaltWall.techNode, MCBlocks.steelWall);
        node(steelWall, MCBlocks.steelWallLarge);

        // Logistique et énergie
        node(Blocks.titaniumConveyor.techNode, MCBlocks.reinforcedConveyor);
        node(Blocks.powerNode.techNode, MCBlocks.aluminumNode);
        node(MCBlocks.reinforcedConveyor.techNode, MCBlocks.platedConveyor);
        node(Blocks.container.techNode, MCBlocks.invarContainer);

        // Unités alliées (les ennemies ne sont pas recherchables)
        node(UnitTypes.dagger.techNode, MCUnits.warden);
        TechNode aid = node(UnitTypes.flare.techNode, MCUnits.aid);
        TechNode sentinel = node(MCUnits.warden.techNode, MCUnits.sentinel);
        node(sentinel, MCUnits.bastion);
        node(aid, MCUnits.relay);
    }

    private static TechNode node(TechNode parent, UnlockableContent content){
        return node(parent, content, content.researchRequirements());
    }

    private static TechNode node(TechNode parent, UnlockableContent content, ItemStack[] requirements){
        if(parent == null) throw new IllegalStateException("Nœud parent introuvable pour " + content.name);
        return new TechNode(parent, content, requirements);
    }
}
