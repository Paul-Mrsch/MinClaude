package minclaude.content;

import arc.struct.Seq;
import mindustry.content.*;
import mindustry.entities.abilities.RepairFieldAbility;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.units.UnitFactory;
import mindustry.world.blocks.units.UnitFactory.UnitPlan;
import mindustry.world.meta.BlockFlag;

import static mindustry.type.ItemStack.with;

/**
 * Unités du mod. Alliées : produites dans les usines vanilla. Ennemies : seulement dans les vagues
 * (voir {@link minclaude.world.WorldSetup}), jamais constructibles par le joueur.
 */
public final class MCUnits{
    public static UnitType warden, aid;
    public static UnitType marauder, wasp;

    public static final Seq<UnitType> allies = new Seq<>(), enemies = new Seq<>(), all = new Seq<>();

    private MCUnits(){}

    public static void load(){
        warden = ally(new UnitType("warden"){{
            constructor = MechUnit::create;
            speed = 0.55f;
            hitSize = 10f;
            health = 320;
            armor = 3f;
            mechFrontSway = 0.55f;
            weapons.add(new Weapon("minclaude-warden-gun"){{
                reload = 18f;
                x = 5f;
                y = 1f;
                top = false;
                ejectEffect = Fx.casing1;
                bullet = new BasicBulletType(3f, 20){{
                    width = 8f;
                    height = 10f;
                    lifetime = 50f;
                }};
            }});
        }});

        aid = ally(new UnitType("aid"){{
            constructor = UnitEntity::create;
            flying = true;
            speed = 2.4f;
            accel = 0.08f;
            drag = 0.04f;
            health = 160;
            hitSize = 9f;
            engineOffset = 5.5f;
            // Drone de soutien : soigne les unités alliées proches, sans arme.
            abilities.add(new RepairFieldAbility(12f, 60f * 3f, 60f));
        }});

        marauder = enemy(new UnitType("marauder"){{
            constructor = MechUnit::create;
            speed = 0.45f;
            hitSize = 12f;
            health = 480;
            armor = 4f;
            weapons.add(new Weapon("minclaude-marauder-cannon"){{
                reload = 40f;
                x = 6f;
                y = 0f;
                top = false;
                shake = 1f;
                ejectEffect = Fx.casing2;
                bullet = new BasicBulletType(2.6f, 28){{
                    width = 10f;
                    height = 13f;
                    lifetime = 60f;
                    splashDamage = 10f;
                    splashDamageRadius = 16f;
                }};
            }});
        }});

        wasp = enemy(new UnitType("wasp"){{
            constructor = UnitEntity::create;
            flying = true;
            speed = 2.6f;
            accel = 0.09f;
            drag = 0.04f;
            health = 200;
            hitSize = 10f;
            engineOffset = 6f;
            circleTarget = true;
            // Comme le flare vanilla : vise d'abord les générateurs.
            targetFlags = new BlockFlag[]{BlockFlag.generator, BlockFlag.turret, null};
            weapons.add(new Weapon(){{
                y = 1f;
                x = 0f;
                reload = 30f;
                shootCone = 15f;
                mirror = false;
                bullet = new BasicBulletType(3f, 14){{
                    width = 6f;
                    height = 8f;
                    lifetime = 45f;
                }};
            }});
        }});

        // Production des alliés dans les usines vanilla.
        ((UnitFactory)Blocks.groundFactory).plans.add(new UnitPlan(warden, 60f * 20, with(Items.silicon, 15, MCItems.nickel, 10)));
        ((UnitFactory)Blocks.airFactory).plans.add(new UnitPlan(aid, 60f * 25, with(Items.silicon, 15, MCItems.aluminum, 10)));
    }

    private static UnitType ally(UnitType type){
        allies.add(type);
        all.add(type);
        return type;
    }

    private static UnitType enemy(UnitType type){
        enemies.add(type);
        all.add(type);
        return type;
    }
}
