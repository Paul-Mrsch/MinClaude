package minclaude.content;

import arc.struct.Seq;
import mindustry.content.*;
import mindustry.entities.abilities.ForceFieldAbility;
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
    public static UnitType warden, aid, sentinel, bastion, relay,
        scout, engineer, citadel, colossus, beacon, sanctum, halo, skiff, corvette, frigate;
    public static UnitType marauder, wasp, ravager, hornet, brute,
        swarmling, sapper, siegebreaker, shocker, juggernaut, stalker, phantom, gunship, dreadwing, warlord, leviathan;

    /** Familles d'ennemis, utilisées par l'adaptation des vagues (V3). */
    public static final Seq<UnitType> airEnemies = new Seq<>(), siegeEnemies = new Seq<>(), armorEnemies = new Seq<>(), swarmEnemies = new Seq<>();

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
            health = 6000;
            armor = 12f;
            mechFrontSway = 0.4f;
            weapons.add(new Weapon("minclaude-brute-cannon"){{
                reload = 65f;
                x = 11f;
                top = false;
                recoil = 5f;
                shake = 3f;
                shootSound = Sounds.shootArtillery;
                ejectEffect = Fx.casing3;
                bullet = new BasicBulletType(2.4f, 95){{
                    width = 14f;
                    height = 18f;
                    lifetime = 70f;
                    splashDamage = 70f;
                    splashDamageRadius = 28f;
                    hitEffect = Fx.blastExplosion;
                }};
            }});
        }});

        // Lignée alliée dans les reconstructeurs vanilla.
        ((Reconstructor)Blocks.additiveReconstructor).upgrades.add(new UnitType[]{warden, sentinel}, new UnitType[]{aid, relay});
        ((Reconstructor)Blocks.multiplicativeReconstructor).upgrades.add(new UnitType[]{sentinel, bastion});

        loadV3();
    }

    /** V3 : lignées T4/T5, navals, éclaireur, ingénieur ; 11 ennemis dont 2 boss. */
    private static void loadV3(){
        // Familles des ennemis V1/V2.
        swarmEnemies.add(marauder);
        airEnemies.add(wasp, hornet);
        armorEnemies.add(ravager, brute);

        // ---- Alliés ----
        scout = ally(mech("scout", 180, 1, 0.8f, 8f, gun("minclaude-scout-gun", 12f, 4f, bullet(3.4f, 8, 7f, 9f, 45f))));
        engineer = ally(mech("engineer", 240, 2, 0.6f, 9f, gun("minclaude-engineer-gun", 20f, 4f, bullet(3f, 7, 6f, 8f, 40f))));
        engineer.buildSpeed = 0.7f;
        engineer.mineTier = 2;
        engineer.mineSpeed = 3f;
        citadel = ally(mech("citadel", 5200, 12, 0.36f, 22f,
            gun("minclaude-citadel-cannon", 45f, 13f, artillery(2.4f, 40, 180f, 120f, 40f))));
        colossus = ally(mech("colossus", 18000, 16, 0.3f, 30f,
            gun("minclaude-colossus-cannon", 60f, 17f, artillery(2.6f, 80, 400f, 130f, 60f))));
        colossus.weapons.first().shoot.shots = 2;

        beacon = ally(flyer("beacon", 900, 4, 2.0f, 16f));
        beacon.abilities.add(new RepairFieldAbility(40f, 60f * 2f, 100f));
        beacon.weapons.add(healGun(20f, 18));
        sanctum = ally(flyer("sanctum", 4500, 8, 1.4f, 26f));
        sanctum.abilities.add(new ForceFieldAbility(90f, 0.5f, 600f, 60f * 6f), new RepairFieldAbility(60f, 60f * 2f, 140f));
        sanctum.weapons.add(healGun(14f, 26));
        halo = ally(flyer("halo", 12000, 12, 1.1f, 36f));
        halo.abilities.add(new ForceFieldAbility(140f, 0.8f, 1400f, 60f * 6f), new RepairFieldAbility(110f, 60f * 2f, 180f));
        halo.weapons.add(healGun(8f, 40));

        skiff = ally(boat("skiff", 280, 2, 1.2f, 10f, gun("minclaude-skiff-gun", 18f, 0f, bullet(2.8f, 10, 7f, 9f, 50f))));
        corvette = ally(boat("corvette", 650, 4, 1f, 14f, gun("minclaude-corvette-gun", 22f, 4f, bullet(3.2f, 20, 8f, 11f, 55f))));
        frigate = ally(boat("frigate", 1300, 7, 0.85f, 18f, gun("minclaude-frigate-mortar", 70f, 0f, artillery(2.2f, 25, 90f, 110f, 35f))));
        frigate.weapons.first().mirror = false;

        // ---- Ennemis ----
        swarmling = enemy(mech("swarmling", 120, 0, 1.0f, 7f, gun(null, 25f, 0f, bullet(2.5f, 10, 5f, 6f, 20f))), swarmEnemies);
        sapper = enemy(mech("sapper", 520, 3, 0.55f, 10f, gun("minclaude-sapper-gun", 30f, 5f, bullet(2.6f, 24, 8f, 10f, 45f))), siegeEnemies);
        sapper.weapons.first().bullet.buildingDamageMultiplier = 3f;
        siegebreaker = enemy(mech("siegebreaker", 1300, 7, 0.38f, 16f,
            gun("minclaude-siegebreaker-mortar", 80f, 8f, artillery(2f, 30, 110f, 150f, 45f))), siegeEnemies);
        shocker = enemy(mech("shocker", 720, 5, 0.48f, 12f, lightning("minclaude-shocker-coil")), armorEnemies);
        juggernaut = enemy(mech("juggernaut", 7000, 14, 0.3f, 22f, gun("minclaude-juggernaut-cannon", 60f, 11f, bullet(2.4f, 120, 14f, 18f, 75f))), armorEnemies);
        juggernaut.abilities.add(new ForceFieldAbility(40f, 0.3f, 400f, 60f * 8f));
        stalker = enemy(mech("stalker", 600, 4, 0.75f, 11f, shotgun("minclaude-stalker-shotgun")), swarmEnemies);
        phantom = enemy(flyer("phantom", 260, 1, 3.2f, 9f), airEnemies);
        phantom.weapons.add(gun(null, 16f, 3f, bullet(4f, 9, 5f, 7f, 30f)));
        gunship = enemy(flyer("gunship", 1100, 5, 1.6f, 18f), airEnemies);
        gunship.weapons.add(gun(null, 10f, 6f, bullet(3.6f, 14, 7f, 9f, 45f)));
        dreadwing = enemy(flyer("dreadwing", 3800, 9, 1.1f, 28f), airEnemies);
        dreadwing.targetAir = false;
        dreadwing.weapons.add(bombs(12f, 60f));
        warlord = enemy(mech("warlord", 22000, 18, 0.27f, 30f,
            gun("minclaude-warlord-cannon", 45f, 16f, artillery(2.5f, 80, 340f, 140f, 60f))), armorEnemies);
        warlord.weapons.add(lightning(null));
        leviathan = enemy(flyer("leviathan", 12000, 14, 0.9f, 40f), airEnemies);
        leviathan.weapons.add(bombs(20f, 120f), gun(null, 8f, 12f, bullet(4f, 22, 8f, 10f, 50f)));

        // Production et améliorations dans les usines vanilla.
        var ground = (UnitFactory)Blocks.groundFactory;
        ground.plans.add(new UnitPlan(scout, 60f * 12, with(Items.silicon, 10, MCItems.aluminum, 5)));
        ground.plans.add(new UnitPlan(engineer, 60f * 25, with(Items.silicon, 20, MCItems.steel, 10)));
        ((UnitFactory)Blocks.navalFactory).plans.add(new UnitPlan(skiff, 60f * 25, with(Items.silicon, 20, Items.metaglass, 25, MCItems.nickel, 10)));
        ((Reconstructor)Blocks.additiveReconstructor).upgrades.add(new UnitType[]{skiff, corvette});
        ((Reconstructor)Blocks.multiplicativeReconstructor).upgrades.add(new UnitType[]{relay, beacon}, new UnitType[]{corvette, frigate});
        ((Reconstructor)Blocks.exponentialReconstructor).upgrades.add(new UnitType[]{bastion, citadel}, new UnitType[]{beacon, sanctum});
        ((Reconstructor)Blocks.tetrativeReconstructor).upgrades.add(new UnitType[]{citadel, colossus}, new UnitType[]{sanctum, halo});
    }

    // ---- Fabriques compactes ----

    private static UnitType mech(String name, float health, float armor, float speed, float hitSize, Weapon... weapons){
        UnitType t = new UnitType(name);
        t.constructor = MechUnit::create;
        t.health = health;
        t.armor = armor;
        t.speed = speed;
        t.hitSize = hitSize;
        t.mechFrontSway = 0.55f;
        t.weapons.addAll(weapons);
        return t;
    }

    private static UnitType flyer(String name, float health, float armor, float speed, float hitSize){
        UnitType t = new UnitType(name);
        t.constructor = UnitEntity::create;
        t.flying = true;
        t.health = health;
        t.armor = armor;
        t.speed = speed;
        t.hitSize = hitSize;
        t.accel = 0.08f;
        t.drag = 0.04f;
        t.engineOffset = hitSize * 0.55f;
        t.engineSize = Math.max(2f, hitSize / 6f);
        return t;
    }

    private static UnitType boat(String name, float health, float armor, float speed, float hitSize, Weapon... weapons){
        UnitType t = new UnitType(name);
        t.constructor = UnitWaterMove::create;
        t.health = health;
        t.armor = armor;
        t.speed = speed;
        t.hitSize = hitSize;
        t.drag = 0.13f;
        t.accel = 0.4f;
        t.rotateSpeed = 3.5f;
        t.trailLength = 18;
        t.waveTrailX = hitSize * 0.4f;
        t.waveTrailY = -hitSize * 0.4f;
        t.weapons.addAll(weapons);
        return t;
    }

    /** Arme à projectile ; {@code sprite} null = arme sans dessin (fixée dans la coque). */
    private static Weapon gun(String sprite, float reload, float x, BulletType bullet){
        Weapon w = sprite == null ? new Weapon() : new Weapon(sprite);
        w.reload = reload;
        w.x = x;
        w.top = false;
        w.mirror = x != 0f;
        w.ejectEffect = Fx.casing1;
        w.bullet = bullet;
        return w;
    }

    private static BasicBulletType bullet(float speed, float damage, float width, float height, float lifetime){
        BasicBulletType b = new BasicBulletType(speed, damage);
        b.width = width;
        b.height = height;
        b.lifetime = lifetime;
        return b;
    }

    private static ArtilleryBulletType artillery(float speed, float damage, float splash, float lifetime, float radius){
        ArtilleryBulletType b = new ArtilleryBulletType(speed, damage, "shell");
        b.splashDamage = splash;
        b.splashDamageRadius = radius;
        b.lifetime = lifetime;
        b.width = b.height = 14f;
        b.collidesTiles = true;
        b.hitEffect = Fx.blastExplosion;
        b.backColor = Pal.bulletYellowBack;
        b.frontColor = Pal.bulletYellow;
        return b;
    }

    private static Weapon healGun(float reload, float damage){
        Weapon w = new Weapon();
        w.x = 0f;
        w.mirror = false;
        w.reload = reload;
        LaserBoltBulletType b = new LaserBoltBulletType(5.2f, damage);
        b.lifetime = 34f;
        b.healPercent = 6f;
        b.collidesTeam = true;
        b.backColor = Pal.heal;
        b.frontColor = Pal.lightishGray;
        w.bullet = b;
        return w;
    }

    private static Weapon bombs(float damage, float splash){
        Weapon w = new Weapon();
        w.minShootVelocity = 0.6f;
        w.x = 3f;
        w.shootY = 0f;
        w.reload = 16f;
        w.shootCone = 180f;
        w.ignoreRotation = true;
        w.inaccuracy = 15f;
        w.ejectEffect = Fx.none;
        BombBulletType b = new BombBulletType(damage, splash * 0.4f);
        b.splashDamage = splash;
        b.width = 10f;
        b.height = 14f;
        b.hitEffect = Fx.flakExplosion;
        b.status = StatusEffects.blasted;
        w.bullet = b;
        return w;
    }

    private static Weapon lightning(String sprite){
        Weapon w = sprite == null ? new Weapon() : new Weapon(sprite);
        w.x = sprite == null ? 0f : 6f;
        w.mirror = sprite != null;
        w.top = false;
        w.reload = 45f;
        LightningBulletType b = new LightningBulletType();
        b.damage = 30f;
        b.lightningLength = 12;
        b.status = StatusEffects.shocked;
        b.lightningColor = Pal.lancerLaser;
        w.bullet = b;
        w.shootSound = Sounds.shootArc;
        return w;
    }

    private static Weapon shotgun(String sprite){
        Weapon w = gun(sprite, 40f, 6f, bullet(3.2f, 12, 6f, 8f, 26f));
        w.shoot.shots = 5;
        w.inaccuracy = 18f;
        w.velocityRnd = 0.2f;
        return w;
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

    private static UnitType enemy(UnitType type, Seq<UnitType> family){
        family.add(type);
        return enemy(type);
    }
}
