package minclaude.content;

import arc.struct.Seq;
import mindustry.content.*;
import mindustry.entities.abilities.RepairFieldAbility;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.graphics.Pal;
import mindustry.world.blocks.units.Reconstructor;
import mindustry.world.blocks.units.UnitFactory;
import mindustry.world.blocks.units.UnitFactory.UnitPlan;
import mindustry.world.meta.BlockFlag;

import static mindustry.type.ItemStack.with;

/**
 * Unités du mod. Alliées : produites dans les usines vanilla. Ennemies : seulement dans les vagues
 * (voir {@link minclaude.world.WorldSetup}), jamais constructibles par le joueur.
 */
public final class MCUnits{
    public static UnitType warden, aid, sentinel, bastion, relay;
    public static UnitType marauder, wasp, ravager, hornet, brute;

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

        loadV2();
    }

    /** V2 : lignée T2/T3 (reconstructeurs vanilla) et nouveaux ennemis. */
    private static void loadV2(){
        sentinel = ally(new UnitType("sentinel"){{
            constructor = MechUnit::create;
            speed = 0.5f;
            hitSize = 13f;
            health = 650;
            armor = 6f;
            mechFrontSway = 0.6f;
            weapons.add(new Weapon("minclaude-sentinel-gun"){{
                reload = 24f;
                x = 7f;
                y = 1f;
                top = false;
                shoot.shots = 3;
                shoot.shotDelay = 4f;
                ejectEffect = Fx.casing1;
                bullet = new BasicBulletType(3.2f, 18){{
                    width = 8f;
                    height = 11f;
                    lifetime = 55f;
                }};
            }});
        }});

        bastion = ally(new UnitType("bastion"){{
            constructor = MechUnit::create;
            speed = 0.42f;
            hitSize = 16f;
            health = 1400;
            armor = 10f;
            mechFrontSway = 0.5f;
            targetAir = false;
            weapons.add(new Weapon("minclaude-bastion-mortar"){{
                reload = 70f;
                x = 9f;
                y = 0f;
                top = false;
                recoil = 4f;
                shake = 2f;
                ejectEffect = Fx.casing2;
                shootSound = Sounds.shootArtillery;
                bullet = new ArtilleryBulletType(2.2f, 25, "shell"){{
                    hitEffect = Fx.blastExplosion;
                    lifetime = 100f;
                    width = height = 14f;
                    collidesTiles = true;
                    splashDamageRadius = 38f;
                    splashDamage = 95f;
                    backColor = Pal.bulletYellowBack;
                    frontColor = Pal.bulletYellow;
                }};
            }});
        }});

        relay = ally(new UnitType("relay"){{
            constructor = UnitEntity::create;
            flying = true;
            speed = 2.2f;
            accel = 0.07f;
            drag = 0.04f;
            health = 420;
            armor = 2f;
            hitSize = 12f;
            engineOffset = 7f;
            abilities.add(new RepairFieldAbility(25f, 60f * 2.5f, 80f));
            weapons.add(new Weapon(){{
                x = 0f;
                reload = 30f;
                mirror = false;
                bullet = new LaserBoltBulletType(5f, 10){{
                    lifetime = 32f;
                    healPercent = 6f;
                    collidesTeam = true;
                    backColor = Pal.heal;
                    frontColor = Pal.lightishGray;
                }};
            }});
        }});

        ravager = enemy(new UnitType("ravager"){{
            constructor = MechUnit::create;
            speed = 0.4f;
            hitSize = 14f;
            health = 950;
            armor = 8f;
            weapons.add(new Weapon("minclaude-ravager-shotgun"){{
                reload = 50f;
                x = 7f;
                top = false;
                shoot.shots = 6;
                inaccuracy = 16f;
                velocityRnd = 0.2f;
                ejectEffect = Fx.casing2;
                bullet = new BasicBulletType(3f, 14){{
                    width = 7f;
                    height = 9f;
                    lifetime = 35f;
                    knockback = 1f;
                }};
            }});
        }});

        hornet = enemy(new UnitType("hornet"){{
            constructor = UnitEntity::create;
            flying = true;
            speed = 1.7f;
            accel = 0.08f;
            drag = 0.016f;
            health = 380;
            armor = 3f;
            hitSize = 13f;
            engineOffset = 7f;
            targetAir = false;
            targetFlags = new BlockFlag[]{BlockFlag.factory, BlockFlag.generator, null};
            weapons.add(new Weapon(){{
                minShootVelocity = 0.8f;
                x = 3f;
                shootY = 0f;
                reload = 18f;
                shootCone = 180f;
                ejectEffect = Fx.none;
                inaccuracy = 15f;
                ignoreRotation = true;
                bullet = new BombBulletType(24f, 22f){{
                    width = 10f;
                    height = 13f;
                    hitEffect = Fx.flakExplosion;
                    shootEffect = Fx.none;
                    smokeEffect = Fx.none;
                    status = StatusEffects.blasted;
                    statusDuration = 60f;
                }};
            }});
        }});

        brute = enemy(new UnitType("brute"){{
            constructor = MechUnit::create;
            speed = 0.33f;
            hitSize = 20f;
            health = 2600;
            armor = 12f;
            mechFrontSway = 0.4f;
            weapons.add(new Weapon("minclaude-brute-cannon"){{
                reload = 90f;
                x = 11f;
                top = false;
                recoil = 5f;
                shake = 3f;
                shootSound = Sounds.shootArtillery;
                ejectEffect = Fx.casing3;
                bullet = new BasicBulletType(2.4f, 60){{
                    width = 14f;
                    height = 18f;
                    lifetime = 70f;
                    splashDamage = 40f;
                    splashDamageRadius = 28f;
                    hitEffect = Fx.blastExplosion;
                }};
            }});
        }});

        // Lignée alliée dans les reconstructeurs vanilla.
        ((Reconstructor)Blocks.additiveReconstructor).upgrades.add(new UnitType[]{warden, sentinel}, new UnitType[]{aid, relay});
        ((Reconstructor)Blocks.multiplicativeReconstructor).upgrades.add(new UnitType[]{sentinel, bastion});
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
