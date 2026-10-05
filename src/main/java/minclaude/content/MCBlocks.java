package minclaude.content;

import arc.struct.Seq;
import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.Block;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.blocks.production.GenericCrafter;

import static mindustry.type.ItemStack.with;

/** Bâtiments du mod. V0 : une industrie et deux murs, pour valider toute la chaîne (code, sprites, traductions, tech tree). */
public final class MCBlocks{
    /** PV d'un mur vanilla par unité de « santé de base » (Blocks.wallHealthMultiplier). */
    private static final int WALL_HEALTH_MULTIPLIER = 4;

    public static Block cobaltSmelter, cobaltWall, cobaltWallLarge;

    public static final Seq<Block> all = new Seq<>();

    private MCBlocks(){}

    public static void load(){
        cobaltSmelter = add(new GenericCrafter("cobalt-smelter"){{
            requirements(Category.crafting, with(Items.copper, 60, Items.lead, 40, Items.graphite, 20));
            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(MCItems.cobalt, 1);
            craftTime = 60f;
            size = 2;
            hasPower = true;
            consumePower(0.6f);
            consumeItems(with(Items.lead, 2, Items.sand, 1));
        }});

        cobaltWall = add(new Wall("cobalt-wall"){{
            requirements(Category.defense, with(MCItems.cobalt, 6));
            health = 130 * WALL_HEALTH_MULTIPLIER;
        }});

        cobaltWallLarge = add(new Wall("cobalt-wall-large"){{
            requirements(Category.defense, ItemStack.mult(cobaltWall.requirements, 4));
            health = 130 * WALL_HEALTH_MULTIPLIER * 4;
            size = 2;
        }});
    }

    private static Block add(Block block){
        all.add(block);
        return block;
    }
}
