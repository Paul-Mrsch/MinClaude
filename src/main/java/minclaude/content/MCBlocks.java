package minclaude.content;

import arc.struct.Seq;
import mindustry.content.*;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.type.*;
import mindustry.world.Block;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.LiquidTurret;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.entities.bullet.LiquidBulletType;
import mindustry.world.blocks.distribution.ArmoredConveyor;
import mindustry.world.blocks.distribution.Conveyor;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.blocks.production.*;

import static mindustry.type.ItemStack.with;

/** Bâtiments du mod : minerais, industries, logistique, défense, énergie. */
public final class MCBlocks{
    /** PV d'un mur vanilla par unité de « santé de base » (Blocks.wallHealthMultiplier). */
    private static final int WALL_HEALTH_MULTIPLIER = 4;

    // Minerais (environnement)
    public static OreBlock oreCobalt, oreNickel, oreZinc, oreBauxite, oreChrome;
    // Industries
    public static Block cobaltSmelter, aluminumSmelter, brassFoundry, oreWasher, percussionDrill,
        steelFurnace, alloyPress, brineMixer, cryogenizer, electrolyzer;
    // Bâtiments
    public static Block cobaltWall, cobaltWallLarge, nickelWall, nickelWallLarge, reinforcedConveyor, rivet, aluminumNode,
        steelWall, steelWallLarge, platedConveyor, volley, frost, invarContainer;

    /** Tous les blocs du mod (tests, générateur de sprites). */
    public static final Seq<Block> all = new Seq<>();
    /** Blocs d'environnement : pas de tech tree ni de coût. */
    public static final Seq<OreBlock> ores = new Seq<>();

    private MCBlocks(){}

    public static void load(){
        oreCobalt = ore("ore-cobalt", MCItems.cobalt, 0.87f, 26f);
        oreNickel = ore("ore-nickel", MCItems.nickel, 0.85f, 24f);
        oreZinc = ore("ore-zinc", MCItems.zinc, 0.85f, 25f);
        oreBauxite = ore("ore-bauxite", MCItems.bauxite, 0.84f, 23f);
        oreChrome = ore("ore-chrome", MCItems.chrome, 0.88f, 27f);

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

        aluminumSmelter = add(new GenericCrafter("aluminum-smelter"){{
            requirements(Category.crafting, with(Items.copper, 50, Items.lead, 35, MCItems.nickel, 20));
            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(MCItems.aluminum, 1);
            craftTime = 50f;
            size = 2;
            hasPower = true;
            ambientSound = Sounds.loopSmelter;
            ambientSoundVolume = 0.06f;
            consumePower(0.8f);
            consumeItems(with(MCItems.bauxite, 2, Items.coal, 1));
        }});

        brassFoundry = add(new GenericCrafter("brass-foundry"){{
            requirements(Category.crafting, with(Items.copper, 40, Items.lead, 30, MCItems.zinc, 25));
            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(MCItems.brass, 2);
            craftTime = 70f;
            size = 2;
            hasPower = true;
            consumePower(0.5f);
            consumeItems(with(Items.copper, 2, MCItems.zinc, 1));
        }});

        oreWasher = add(new Separator("ore-washer"){{
            requirements(Category.crafting, with(Items.copper, 35, Items.lead, 30, Items.graphite, 15));
            results = with(MCItems.nickel, 4, MCItems.zinc, 3, MCItems.bauxite, 3, MCItems.cobalt, 1);
            craftTime = 40f;
            size = 2;
            hasPower = true;
            consumePower(0.7f);
            consumeItem(Items.sand, 1);
            consumeLiquid(Liquids.water, 6f / 60f);
        }});

        percussionDrill = add(new Drill("percussion-drill"){{
            requirements(Category.production, with(Items.copper, 30, Items.graphite, 15, MCItems.nickel, 15));
            tier = 3;
            drillTime = 320;
            size = 2;
            hasPower = true;
            consumePower(0.4f);
            consumeLiquid(Liquids.water, 4f / 60f).boost();
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

        nickelWall = add(new Wall("nickel-wall"){{
            requirements(Category.defense, with(MCItems.nickel, 6));
            health = 95 * WALL_HEALTH_MULTIPLIER;
        }});

        nickelWallLarge = add(new Wall("nickel-wall-large"){{
            requirements(Category.defense, ItemStack.mult(nickelWall.requirements, 4));
            health = 95 * WALL_HEALTH_MULTIPLIER * 4;
            size = 2;
        }});

        reinforcedConveyor = add(new Conveyor("reinforced-conveyor"){{
            requirements(Category.distribution, with(Items.copper, 1, Items.lead, 1, MCItems.aluminum, 1));
            health = 90;
            speed = 0.1f;
            displayedSpeed = 13f;
        }});

        rivet = add(new ItemTurret("rivet"){{
            requirements(Category.turret, with(Items.copper, 60, Items.graphite, 25, MCItems.nickel, 30));
            ammo(
                MCItems.nickel, new BasicBulletType(3f, 18){{
                    width = 8f;
                    height = 11f;
                    lifetime = 55f;
                    ammoMultiplier = 2;
                    hitColor = backColor = trailColor = MCItems.nickel.color.cpy().mul(0.8f);
                    frontColor = MCItems.nickel.color;
                }},
                MCItems.aluminum, new BasicBulletType(4f, 12){{
                    width = 6f;
                    height = 10f;
                    lifetime = 42f;
                    ammoMultiplier = 3;
                    reloadMultiplier = 1.5f;
                    hitColor = backColor = trailColor = Pal.gray;
                    frontColor = MCItems.aluminum.color;
                }},
                MCItems.brass, new BasicBulletType(3f, 26){{
                    width = 9f;
                    height = 12f;
                    lifetime = 55f;
                    ammoMultiplier = 2;
                    pierceCap = 2;
                    pierce = true;
                    hitColor = backColor = trailColor = MCItems.brass.color.cpy().mul(0.8f);
                    frontColor = MCItems.brass.color;
                }}
            );
            size = 2;
            range = 150f;
            reload = 22f;
            recoil = 1.5f;
            shootCone = 12f;
            health = 380;
            rotateSpeed = 9f;
            shootSound = Sounds.shootDuo;
            coolant = consumeCoolant(0.2f);
            limitRange();
        }});

        aluminumNode = add(new PowerNode("aluminum-node"){{
            requirements(Category.power, with(Items.lead, 4, MCItems.aluminum, 3));
            maxNodes = 12;
            laserRange = 9f;
        }});

        loadV2();
    }

    /** V2 : acier, invar, chrome, liquides, défense et logistique avancées. */
    private static void loadV2(){
        steelFurnace = add(new GenericCrafter("steel-furnace"){{
            requirements(Category.crafting, with(Items.copper, 70, Items.graphite, 40, MCItems.nickel, 30));
            craftEffect = Fx.smeltsmoke;
            outputItem = new ItemStack(MCItems.steel, 1);
            craftTime = 75f;
            size = 2;
            hasPower = true;
            ambientSound = Sounds.loopSmelter;
            ambientSoundVolume = 0.07f;
            consumePower(1f);
            consumeItems(with(Items.scrap, 2, Items.coal, 1));
        }});

        alloyPress = add(new GenericCrafter("alloy-press"){{
            requirements(Category.crafting, with(Items.lead, 60, Items.silicon, 30, MCItems.steel, 30));
            craftEffect = Fx.pulverizeMedium;
            outputItem = new ItemStack(MCItems.invar, 2);
            craftTime = 90f;
            size = 2;
            hasPower = true;
            consumePower(1.2f);
            consumeItems(with(MCItems.nickel, 2, MCItems.steel, 1));
        }});

        brineMixer = add(new GenericCrafter("brine-mixer"){{
            requirements(Category.crafting, with(Items.copper, 40, Items.metaglass, 25, MCItems.zinc, 20));
            outputLiquid = new LiquidStack(MCLiquids.brine, 10f / 60f);
            craftTime = 60f;
            size = 2;
            hasPower = true;
            hasLiquids = true;
            outputsLiquid = true;
            liquidCapacity = 30f;
            consumePower(0.5f);
            consumeItem(Items.sand, 1);
            consumeLiquid(Liquids.water, 10f / 60f);
        }});

        cryogenizer = add(new GenericCrafter("cryogenizer"){{
            requirements(Category.crafting, with(Items.lead, 50, Items.silicon, 40, MCItems.aluminum, 40));
            outputLiquid = new LiquidStack(MCLiquids.nitrogen, 12f / 60f);
            craftTime = 120f;
            size = 2;
            hasPower = true;
            hasLiquids = true;
            outputsLiquid = true;
            liquidCapacity = 30f;
            consumePower(1.5f);
            consumeItem(MCItems.aluminum, 1);
            consumeLiquid(Liquids.water, 12f / 60f);
        }});

        electrolyzer = add(new GenericCrafter("brine-electrolyzer"){{
            requirements(Category.crafting, with(Items.copper, 60, Items.silicon, 40, MCItems.brass, 30));
            craftEffect = Fx.pulverizeMedium;
            outputItem = new ItemStack(MCItems.chrome, 1);
            craftTime = 80f;
            size = 2;
            hasPower = true;
            hasLiquids = true;
            consumePower(1.8f);
            consumeLiquid(MCLiquids.brine, 12f / 60f);
        }});

        steelWall = add(new Wall("steel-wall"){{
            requirements(Category.defense, with(MCItems.steel, 6));
            health = 160 * WALL_HEALTH_MULTIPLIER;
            absorbLasers = true;
        }});

        steelWallLarge = add(new Wall("steel-wall-large"){{
            requirements(Category.defense, ItemStack.mult(steelWall.requirements, 4));
            health = 160 * WALL_HEALTH_MULTIPLIER * 4;
            size = 2;
            absorbLasers = true;
        }});

        platedConveyor = add(new ArmoredConveyor("plated-conveyor"){{
            requirements(Category.distribution, with(MCItems.invar, 1, MCItems.aluminum, 1, Items.metaglass, 1));
            health = 260;
            speed = 0.11f;
            displayedSpeed = 14.5f;
        }});

        volley = add(new ItemTurret("volley"){{
            requirements(Category.turret, with(Items.copper, 100, Items.graphite, 60, MCItems.steel, 50));
            ammo(
                MCItems.steel, new BasicBulletType(3.5f, 16){{
                    width = 7f;
                    height = 10f;
                    lifetime = 40f;
                    ammoMultiplier = 3;
                    hitColor = backColor = trailColor = MCItems.steel.color.cpy().mul(0.8f);
                    frontColor = Pal.lightishGray;
                }},
                MCItems.invar, new BasicBulletType(3.5f, 22){{
                    width = 8f;
                    height = 11f;
                    lifetime = 40f;
                    ammoMultiplier = 2;
                    knockback = 1.2f;
                    hitColor = backColor = trailColor = MCItems.invar.color.cpy().mul(0.8f);
                    frontColor = MCItems.invar.color;
                }}
            );
            // Fusil : une salve de 5 balles en éventail.
            shoot.shots = 5;
            inaccuracy = 14f;
            velocityRnd = 0.15f;
            size = 2;
            range = 130f;
            reload = 45f;
            recoil = 2.5f;
            shootCone = 25f;
            health = 620;
            rotateSpeed = 7f;
            shootSound = Sounds.shootDuo;
            coolant = consumeCoolant(0.3f);
            limitRange();
        }});

        frost = add(new LiquidTurret("frost"){{
            requirements(Category.turret, with(Items.metaglass, 60, Items.lead, 70, MCItems.aluminum, 40));
            ammo(
                MCLiquids.nitrogen, new LiquidBulletType(MCLiquids.nitrogen){{
                    damage = 6f;
                    drag = 0.01f;
                    status = StatusEffects.freezing;
                    statusDuration = 240f;
                }},
                Liquids.cryofluid, new LiquidBulletType(Liquids.cryofluid){{
                    damage = 3f;
                    drag = 0.01f;
                }},
                Liquids.water, new LiquidBulletType(Liquids.water){{
                    knockback = 0.6f;
                    drag = 0.01f;
                }}
            );
            size = 2;
            recoil = 0f;
            reload = 4f;
            inaccuracy = 4f;
            shootCone = 45f;
            liquidCapacity = 20f;
            range = 120f;
            health = 500;
            targetAir = true;
        }});

        invarContainer = add(new StorageBlock("invar-container"){{
            requirements(Category.effect, with(MCItems.invar, 60, MCItems.steel, 30));
            size = 2;
            itemCapacity = 450;
            health = 900;
            coreMerge = false;
        }});
    }

    private static OreBlock ore(String name, Item item, float threshold, float scale){
        OreBlock ore = new OreBlock(name, item){{
            // Utilisé par la génération par défaut des cartes personnalisées (éditeur).
            oreDefault = true;
            oreThreshold = threshold;
            oreScale = scale;
        }};
        ores.add(ore);
        all.add(ore);
        return ore;
    }

    private static Block add(Block block){
        all.add(block);
        return block;
    }
}
