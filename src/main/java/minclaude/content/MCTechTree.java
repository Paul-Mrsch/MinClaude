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
        node(Liquids.water.techNode, MCLiquids.acid, ItemStack.empty);
        TechNode duralumin = node(MCItems.aluminum.techNode, MCItems.duralumin, ItemStack.empty);
        node(duralumin, MCItems.cermet, ItemStack.empty);
        node(Items.plastanium.techNode, MCItems.carbonFiber, ItemStack.empty);
        node(Items.phaseFabric.techNode, MCItems.quantumCrystal, ItemStack.empty);

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
        node(furnace, MCBlocks.duraluminForge);
        TechNode acid = node(mixer, MCBlocks.acidPlant);
        TechNode kiln = node(acid, MCBlocks.cermetKiln);
        node(acid, MCBlocks.carbonWeaver);
        node(kiln, MCBlocks.quantumResonator);

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
        TechNode cermetWall = node(steelWall, MCBlocks.cermetWall);
        node(cermetWall, MCBlocks.cermetWallLarge);
        node(Blocks.ripple.techNode, MCBlocks.railgun);

        // Logistique et énergie
        node(Blocks.titaniumConveyor.techNode, MCBlocks.reinforcedConveyor);
        node(Blocks.powerNode.techNode, MCBlocks.aluminumNode);
        node(MCBlocks.reinforcedConveyor.techNode, MCBlocks.platedConveyor);
        node(Blocks.container.techNode, MCBlocks.invarContainer);
        node(Blocks.itemBridge.techNode, MCBlocks.duraluminBridge);

        // Unités alliées (les ennemies ne sont pas recherchables)
        node(UnitTypes.dagger.techNode, MCUnits.warden);
        TechNode aid = node(UnitTypes.flare.techNode, MCUnits.aid);
        TechNode sentinel = node(MCUnits.warden.techNode, MCUnits.sentinel);
        node(sentinel, MCUnits.bastion);
        TechNode relay = node(aid, MCUnits.relay);
        TechNode scout = node(MCUnits.warden.techNode, MCUnits.scout);
        node(scout, MCUnits.engineer);
        TechNode citadel = node(MCUnits.bastion.techNode, MCUnits.citadel);
        node(citadel, MCUnits.colossus);
        TechNode beacon = node(relay, MCUnits.beacon);
        TechNode sanctum = node(beacon, MCUnits.sanctum);
        node(sanctum, MCUnits.halo);
        TechNode skiff = node(UnitTypes.risso.techNode, MCUnits.skiff);
        TechNode corvette = node(skiff, MCUnits.corvette);
        node(corvette, MCUnits.frigate);
    }

    private static TechNode node(TechNode parent, UnlockableContent content){
        return node(parent, content, content.researchRequirements());
    }

    private static TechNode node(TechNode parent, UnlockableContent content, ItemStack[] requirements){
        if(parent == null) throw new IllegalStateException("Nœud parent introuvable pour " + content.name);
        return new TechNode(parent, content, requirements);
    }
}
