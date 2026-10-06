package minclaude.tools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Génère les sprites pixel art du mod, de façon déterministe (même entrée = mêmes pixels).
 * Usage : {@code ./gradlew generateSprites}. Chaque contenu du mod doit avoir ses sprites (vérifié par ContentIT).
 *
 * <p>Style « mod moderne » : aplats nets par facette (pas de dégradé), angles coupés à 45°, biseaux clairs
 * en haut à gauche et sombres en bas à droite, motifs symétriques, bandes de couleur vive sur métal gris-bleu,
 * rampes de couleurs avec décalage de teinte (ombres froides, lumières chaudes).
 * Orientation du jeu : convoyeurs vers la droite, tourelles, unités et armes vers le haut.
 */
public final class SpriteGenerator{
    enum Kind{ GEM, NUGGET, INGOT, LIQUID, ORE, WALL, CRAFTER, FOUNDRY, WASHER, PRESS, TANK, CRYO, ELECTRO, DRILL, CONVEYOR, ARMORED_CONVEYOR,
        TURRET, SHOTGUN, NOZZLE, CONTAINER, NODE, MECH, FLYER, BOMBER, WEAPON,
        FIBER, WEAVER, RESONATOR, BRIDGE, RAIL, NAVAL }

    /** @param size taille en pixels (côté) */
    record Spec(String folder, String name, Kind kind, int size, Color color){}

    private static final Color COBALT = hex("4a7cf0"), NICKEL = hex("c9c29a"), ZINC = hex("8fc3d6"), BAUXITE = hex("c4643a"),
        ALUMINUM = hex("dde6ee"), BRASS = hex("e8b844"), ALLY = hex("ffc857"), ENEMY = hex("f0503c"), WATER = hex("4fa3ff"), HEAL = hex("6ee6a0"),
        CHROME = hex("c3d2e6"), STEEL = hex("93a0b3"), INVAR = hex("aebb9c"), BRINE = hex("7fb9c9"), NITROGEN = hex("bfeaff"),
        DURALUMIN = hex("b4c6d8"), CERMET = hex("d08c58"), CARBON = hex("5a6070"), QUANTUM = hex("b26bff"), ACID = hex("d6e04a"), BOSS = hex("ff2f5a");

    /** Une ligne par contenu. Nom = nom interne sans le préfixe du mod. */
    static final List<Spec> SPECS = List.of(
        new Spec("items", "cobalt", Kind.GEM, 32, COBALT),
        new Spec("items", "nickel", Kind.NUGGET, 32, NICKEL),
        new Spec("items", "zinc", Kind.GEM, 32, ZINC),
        new Spec("items", "bauxite", Kind.NUGGET, 32, BAUXITE),
        new Spec("items", "aluminum", Kind.INGOT, 32, ALUMINUM),
        new Spec("items", "brass", Kind.INGOT, 32, BRASS),

        new Spec("blocks/environment", "ore-cobalt", Kind.ORE, 32, COBALT),
        new Spec("blocks/environment", "ore-nickel", Kind.ORE, 32, NICKEL),
        new Spec("blocks/environment", "ore-zinc", Kind.ORE, 32, ZINC),
        new Spec("blocks/environment", "ore-bauxite", Kind.ORE, 32, BAUXITE),

        new Spec("blocks", "cobalt-smelter", Kind.CRAFTER, 64, COBALT),
        new Spec("blocks", "aluminum-smelter", Kind.CRAFTER, 64, ALUMINUM),
        new Spec("blocks", "brass-foundry", Kind.FOUNDRY, 64, BRASS),
        new Spec("blocks", "ore-washer", Kind.WASHER, 64, WATER),
        new Spec("blocks", "percussion-drill", Kind.DRILL, 64, NICKEL),
        new Spec("blocks", "cobalt-wall", Kind.WALL, 32, COBALT),
        new Spec("blocks", "cobalt-wall-large", Kind.WALL, 64, COBALT),
        new Spec("blocks", "nickel-wall", Kind.WALL, 32, NICKEL),
        new Spec("blocks", "nickel-wall-large", Kind.WALL, 64, NICKEL),
        new Spec("blocks", "reinforced-conveyor", Kind.CONVEYOR, 32, ALUMINUM),
        new Spec("blocks", "rivet", Kind.TURRET, 64, NICKEL),
        new Spec("blocks", "aluminum-node", Kind.NODE, 32, hex("ffd37f")),

        new Spec("units", "warden", Kind.MECH, 40, ALLY),
        new Spec("units", "aid", Kind.FLYER, 40, HEAL),
        new Spec("units", "marauder", Kind.MECH, 48, ENEMY),
        new Spec("units", "wasp", Kind.FLYER, 40, ENEMY),
        new Spec("units/weapons", "warden-gun", Kind.WEAPON, 24, ALLY),
        new Spec("units/weapons", "marauder-cannon", Kind.WEAPON, 28, ENEMY),

        // ---- V2 ----
        new Spec("items", "chrome", Kind.GEM, 32, CHROME),
        new Spec("items", "steel", Kind.INGOT, 32, STEEL),
        new Spec("items", "invar", Kind.INGOT, 32, INVAR),
        new Spec("liquids", "brine", Kind.LIQUID, 32, BRINE),
        new Spec("liquids", "liquid-nitrogen", Kind.LIQUID, 32, NITROGEN),
        new Spec("blocks/environment", "ore-chrome", Kind.ORE, 32, CHROME),
        new Spec("blocks", "steel-furnace", Kind.CRAFTER, 64, hex("ff8a3d")),
        new Spec("blocks", "alloy-press", Kind.PRESS, 64, INVAR),
        new Spec("blocks", "brine-mixer", Kind.TANK, 64, BRINE),
        new Spec("blocks", "cryogenizer", Kind.CRYO, 64, NITROGEN),
        new Spec("blocks", "brine-electrolyzer", Kind.ELECTRO, 64, BRINE),
        new Spec("blocks", "steel-wall", Kind.WALL, 32, STEEL),
        new Spec("blocks", "steel-wall-large", Kind.WALL, 64, STEEL),
        new Spec("blocks", "plated-conveyor", Kind.ARMORED_CONVEYOR, 32, INVAR),
        new Spec("blocks", "volley", Kind.SHOTGUN, 64, STEEL),
        new Spec("blocks", "frost", Kind.NOZZLE, 64, NITROGEN),
        new Spec("blocks", "invar-container", Kind.CONTAINER, 64, INVAR),
        new Spec("units", "sentinel", Kind.MECH, 48, ALLY),
        new Spec("units", "bastion", Kind.MECH, 56, ALLY),
        new Spec("units", "relay", Kind.FLYER, 48, HEAL),
        new Spec("units", "ravager", Kind.MECH, 52, ENEMY),
        new Spec("units", "hornet", Kind.BOMBER, 48, ENEMY),
        new Spec("units", "brute", Kind.MECH, 64, hex("c2362a")),
        new Spec("units/weapons", "sentinel-gun", Kind.WEAPON, 28, ALLY),
        new Spec("units/weapons", "bastion-mortar", Kind.WEAPON, 32, ALLY),
        new Spec("units/weapons", "ravager-shotgun", Kind.WEAPON, 30, ENEMY),
        new Spec("units/weapons", "brute-cannon", Kind.WEAPON, 36, ENEMY),

        // ---- V3 ----
        new Spec("items", "duralumin", Kind.INGOT, 32, DURALUMIN),
        new Spec("items", "cermet", Kind.NUGGET, 32, CERMET),
        new Spec("items", "carbon-fiber", Kind.FIBER, 32, CARBON),
        new Spec("items", "quantum-crystal", Kind.GEM, 32, QUANTUM),
        new Spec("liquids", "sulfuric-acid", Kind.LIQUID, 32, ACID),
        new Spec("blocks", "duralumin-forge", Kind.CRAFTER, 64, DURALUMIN),
        new Spec("blocks", "acid-plant", Kind.TANK, 64, ACID),
        new Spec("blocks", "cermet-kiln", Kind.FOUNDRY, 64, CERMET),
        new Spec("blocks", "carbon-weaver", Kind.WEAVER, 64, CARBON),
        new Spec("blocks", "quantum-resonator", Kind.RESONATOR, 96, QUANTUM),
        new Spec("blocks", "cermet-wall", Kind.WALL, 32, CERMET),
        new Spec("blocks", "cermet-wall-large", Kind.WALL, 64, CERMET),
        new Spec("blocks", "duralumin-bridge", Kind.BRIDGE, 32, DURALUMIN),
        new Spec("blocks", "railgun", Kind.RAIL, 96, QUANTUM),
        new Spec("units", "scout", Kind.MECH, 36, ALLY),
        new Spec("units", "engineer", Kind.MECH, 36, hex("7fd0ff")),
        new Spec("units", "citadel", Kind.MECH, 72, ALLY),
        new Spec("units", "colossus", Kind.MECH, 88, ALLY),
        new Spec("units", "beacon", Kind.FLYER, 56, HEAL),
        new Spec("units", "sanctum", Kind.FLYER, 72, HEAL),
        new Spec("units", "halo", Kind.FLYER, 96, HEAL),
        new Spec("units", "skiff", Kind.NAVAL, 40, ALLY),
        new Spec("units", "corvette", Kind.NAVAL, 52, ALLY),
        new Spec("units", "frigate", Kind.NAVAL, 64, ALLY),
        new Spec("units", "swarmling", Kind.MECH, 28, ENEMY),
        new Spec("units", "sapper", Kind.MECH, 40, hex("ff8a3d")),
        new Spec("units", "siegebreaker", Kind.MECH, 56, ENEMY),
        new Spec("units", "shocker", Kind.MECH, 44, hex("9b7bff")),
        new Spec("units", "juggernaut", Kind.MECH, 72, hex("c2362a")),
        new Spec("units", "stalker", Kind.MECH, 44, ENEMY),
        new Spec("units", "phantom", Kind.FLYER, 32, hex("ff7a9a")),
        new Spec("units", "gunship", Kind.FLYER, 56, ENEMY),
        new Spec("units", "dreadwing", Kind.BOMBER, 72, hex("c2362a")),
        new Spec("units", "warlord", Kind.MECH, 96, BOSS),
        new Spec("units", "leviathan", Kind.BOMBER, 104, BOSS),
        new Spec("units/weapons", "scout-gun", Kind.WEAPON, 20, ALLY),
        new Spec("units/weapons", "engineer-gun", Kind.WEAPON, 20, hex("7fd0ff")),
        new Spec("units/weapons", "citadel-cannon", Kind.WEAPON, 40, ALLY),
        new Spec("units/weapons", "colossus-cannon", Kind.WEAPON, 48, ALLY),
        new Spec("units/weapons", "skiff-gun", Kind.WEAPON, 20, ALLY),
        new Spec("units/weapons", "corvette-gun", Kind.WEAPON, 24, ALLY),
        new Spec("units/weapons", "frigate-mortar", Kind.WEAPON, 28, ALLY),
        new Spec("units/weapons", "sapper-gun", Kind.WEAPON, 24, hex("ff8a3d")),
        new Spec("units/weapons", "siegebreaker-mortar", Kind.WEAPON, 32, ENEMY),
        new Spec("units/weapons", "shocker-coil", Kind.WEAPON, 26, hex("9b7bff")),
        new Spec("units/weapons", "juggernaut-cannon", Kind.WEAPON, 36, hex("c2362a")),
        new Spec("units/weapons", "stalker-shotgun", Kind.WEAPON, 26, ENEMY),
        new Spec("units/weapons", "warlord-cannon", Kind.WEAPON, 48, BOSS)
    );

    private static final Color OUTLINE = hex("23232b");
    /** Métal gris-bleu, du plus sombre au plus clair. */
    private static final Color[] M = {hex("2c2d35"), hex("43454f"), hex("5e6170"), hex("80849a"), hex("a5a9bb"), hex("ccd0dc")};

    public static void main(String[] args) throws IOException{
        File out = new File(args.length > 0 ? args[0] : "assets/sprites");
        for(Spec spec : SPECS){
            for(Map.Entry<String, BufferedImage> e : render(spec).entrySet()){
                File f = new File(new File(out, spec.folder), e.getKey() + ".png");
                f.getParentFile().mkdirs();
                ImageIO.write(e.getValue(), "png", f);
            }
            System.out.println("sprite " + spec.folder + "/" + spec.name);
        }
        if(args.length > 1){
            ImageIO.write(icon(), "png", new File(args[1]));
            System.out.println("icon " + args[1]);
        }
    }

    /** Blocs qui reçoivent une région « -glow » (doit correspondre à MCBlocks.glowing). */
    static final Set<String> GLOWING = Set.of("cobalt-smelter", "aluminum-smelter", "brass-foundry", "steel-furnace", "alloy-press",
        "brine-electrolyzer", "duralumin-forge", "cermet-kiln", "quantum-resonator");

    /** Nom de fichier (sans .png) -> image. Certains contenus demandent plusieurs régions. */
    static Map<String, BufferedImage> render(Spec spec){
        Map<String, BufferedImage> out = renderBase(spec);
        if(GLOWING.contains(spec.name)) out.put(spec.name + "-glow", glow(spec));
        if(spec.kind == Kind.TURRET || spec.kind == Kind.SHOTGUN || spec.kind == Kind.RAIL) out.put(spec.name + "-heat", heat(spec));
        return out;
    }

    /**
     * Région de lueur : blanc sur transparent, teinté et pulsé par le jeu en mode additif. Elle reprend les zones
     * « chaudes » du sprite : creusets, fente de la presse, arcs, cristal.
     */
    static BufferedImage glow(Spec spec){
        Canvas c = new Canvas(spec.size);
        int m = spec.size / 2;
        Color w = Color.WHITE, soft = new Color(255, 255, 255, 140);
        switch(spec.kind){
            case CRAFTER -> {
                c.octagon(soft, m, m, 9);
                c.octagon(w, m, m, 5);
            }
            case FOUNDRY -> {
                for(int dx : new int[]{-12, 12}){
                    c.octagon(soft, m + dx, m, 6);
                    c.octagon(w, m + dx, m, 3);
                }
            }
            case PRESS -> {
                c.rect(soft, 14, m - 2, spec.size - 28, 4);
                c.rect(w, 18, m - 1, spec.size - 36, 2);
            }
            case ELECTRO -> {
                for(int i = 0; i < 3; i++){
                    int y = m - 8 + i * 8;
                    c.line(m - 6, y, m - 2, y + 3, w);
                    c.line(m - 2, y + 3, m + 3, y, w);
                }
            }
            case RESONATOR -> {
                c.poly(soft, m, m - 16, m - 8, m - 6, m - 8, m + 13, m + 8, m + 13, m + 8, m - 6);
                for(int[] e : new int[][]{{m, 14}, {m, spec.size - 15}, {14, m}, {spec.size - 15, m}}) c.octagon(w, e[0], e[1], 2);
            }
            default -> c.octagon(soft, m, m, 6);
        }
        return c.img;
    }

    /** Région de chaleur des tourelles : les canons, teintés en rouge par le jeu après chaque tir. */
    static BufferedImage heat(Spec spec){
        Canvas c = new Canvas(spec.size);
        int m = spec.size / 2;
        switch(spec.kind){
            case TURRET -> {
                for(int bx : new int[]{m - 7, m + 3}) c.rect(Color.WHITE, bx, 2, 4, 14);
            }
            case SHOTGUN -> c.rect(Color.WHITE, m - 12, 4, 24, 10);
            default -> {
                c.rect(Color.WHITE, m - 11, 2, 4, 30);
                c.rect(Color.WHITE, m + 7, 2, 4, 30);
            }
        }
        return c.img;
    }

    static Map<String, BufferedImage> renderBase(Spec spec){
        Map<String, BufferedImage> out = new LinkedHashMap<>();
        Color[] r = ramp(spec.color);
        switch(spec.kind){
            case GEM -> out.put(spec.name, gem(spec.size, r, spec.name.hashCode()));
            case NUGGET -> out.put(spec.name, nugget(spec.size, r));
            case INGOT -> out.put(spec.name, ingot(spec.size, r));
            case LIQUID -> out.put(spec.name, liquid(spec.size, r));
            case PRESS -> out.put(spec.name, press(spec.size, r));
            case TANK -> out.put(spec.name, tank(spec.size, r, false));
            case CRYO -> out.put(spec.name, tank(spec.size, r, true));
            case ELECTRO -> out.put(spec.name, electrolyzer(spec.size, r));
            case SHOTGUN -> out.put(spec.name, shotgun(spec.size, r));
            case NOZZLE -> out.put(spec.name, nozzle(spec.size, r));
            case CONTAINER -> out.put(spec.name, container(spec.size, r));
            case BOMBER -> out.put(spec.name, bomber(spec.size, r));
            case FIBER -> out.put(spec.name, fiber(spec.size, r));
            case WEAVER -> out.put(spec.name, weaver(spec.size, r));
            case RESONATOR -> out.put(spec.name, resonator(spec.size, r));
            case RAIL -> out.put(spec.name, railgun(spec.size, r));
            case NAVAL -> out.put(spec.name, naval(spec.size, r));
            case BRIDGE -> {
                out.put(spec.name, bridgeBase(spec.size, r, 12));
                out.put(spec.name + "-end", bridgeBase(spec.size, r, 8));
                out.put(spec.name + "-bridge", bridgeBeam(spec.size, r));
                out.put(spec.name + "-arrow", bridgeArrow(spec.size, r));
            }
            case ARMORED_CONVEYOR -> {
                for(int shape = 0; shape < 7; shape++){
                    for(int frame = 0; frame < 4; frame++) out.put(spec.name + "-" + shape + "-" + frame, conveyor(spec.size, r, Math.min(shape, 4), frame, true));
                }
                out.put(spec.name, conveyor(spec.size, r, 0, 0, true));
            }
            case ORE -> {
                for(int v = 1; v <= 3; v++) out.put(spec.name + v, ore(spec.size, r, new Random(spec.name.hashCode() * 31L + v)));
            }
            case WALL -> out.put(spec.name, wall(spec.size, r));
            case CRAFTER -> out.put(spec.name, crafter(spec.size, r, 1));
            case FOUNDRY -> out.put(spec.name, crafter(spec.size, r, 2));
            case WASHER -> out.put(spec.name, washer(spec.size, r));
            case DRILL -> {
                out.put(spec.name, drillBase(spec.size, r));
                out.put(spec.name + "-rotator", drillRotator(spec.size));
                out.put(spec.name + "-top", drillTop(spec.size, r));
            }
            case CONVEYOR -> {
                // 7 formes x 4 images d'animation, nommées comme le vanilla : name-forme-image.
                for(int shape = 0; shape < 7; shape++){
                    for(int frame = 0; frame < 4; frame++) out.put(spec.name + "-" + shape + "-" + frame, conveyor(spec.size, r, Math.min(shape, 4), frame, false));
                }
                out.put(spec.name, conveyor(spec.size, r, 0, 0, false));
            }
            case TURRET -> out.put(spec.name, turret(spec.size, r));
            case NODE -> out.put(spec.name, node(spec.size, r));
            case MECH -> {
                out.put(spec.name, mechBody(spec.size, r));
                out.put(spec.name + "-leg", mechLeg(spec.size));
                out.put(spec.name + "-base", mechBase(spec.size));
            }
            case FLYER -> out.put(spec.name, flyer(spec.size, r));
            case WEAPON -> out.put(spec.name, weapon(spec.size, r));
        }
        return out;
    }

    // ================= Ressources =================

    /** Amas de cristaux prismatiques : face gauche claire, face droite sombre, pointe très claire. */
    private static BufferedImage gem(int size, Color[] r, int seed){
        Canvas c = new Canvas(size);
        Random rand = new Random(seed);
        int[][] crystals = {{9, 28, 17, 9, -2}, {23, 28, 20, 10, 2}, {16, 30, 27, 12, 0}};
        for(int[] k : crystals){
            int bx = k[0] + rand.nextInt(2), by = k[1], h = k[2], w = k[3], tilt = k[4];
            int tx = bx + tilt, ty = by - h, sh = by - h * 2 / 3;
            c.poly(r[3], tx, ty, bx - w / 2, sh, bx - w / 2, by, bx, by);
            c.poly(r[1], tx, ty, bx, by, bx + w / 2, by, bx + w / 2, sh);
            c.poly(r[4], tx, ty, bx - w / 2, sh, bx - w / 4, sh + 2, bx, sh - 1);
            c.poly(r[2], tx, ty, bx, sh - 1, bx + w / 4, sh + 2, bx + w / 2, sh);
            c.set(tx - 1, ty + 3, r[5]);
            c.set(bx - w / 2 + 1, by - 2, r[2]);
        }
        c.outline();
        return c.img;
    }

    /** Pépites arrondies (minerai brut) : volume par aplats, point de lumière, ombre portée. */
    private static BufferedImage nugget(int size, Color[] r){
        Canvas c = new Canvas(size);
        pebble(c, 11, 19, 7, r);
        pebble(c, 21, 13, 6, r);
        pebble(c, 20, 23, 5, r);
        c.outline();
        return c.img;
    }

    private static void pebble(Canvas c, int cx, int cy, int rad, Color[] r){
        c.octagon(r[0], cx + 1, cy + 1, rad);
        c.octagon(r[1], cx, cy, rad);
        c.octagon(r[2], cx - 1, cy - 1, rad - 2);
        c.octagon(r[3], cx - 2, cy - 2, Math.max(1, rad - 4));
        c.set(cx - rad / 2 - 1, cy - rad / 2 - 1, r[5]);
    }

    /** Lingot en perspective : dessus clair, face avant, flanc sombre, arête lumineuse ; un second lingot derrière. */
    private static BufferedImage ingot(int size, Color[] r){
        Canvas c = new Canvas(size);
        bar(c, 5, 7, r);
        bar(c, 2, 15, r);
        c.outline();
        return c.img;
    }

    private static void bar(Canvas c, int x, int y, Color[] r){
        c.poly(r[4], x + 5, y, x + 23, y, x + 26, y + 5, x + 2, y + 5);       // dessus
        c.poly(r[2], x + 2, y + 5, x + 26, y + 5, x + 25, y + 12, x + 3, y + 12); // face avant
        c.poly(r[1], x + 26, y + 5, x + 28, y + 2, x + 28, y + 9, x + 25, y + 12); // flanc
        c.rect(r[5], x + 6, y + 1, 11, 1);
        c.rect(r[3], x + 3, y + 6, 22, 1);
        c.rect(r[1], x + 4, y + 10, 20, 1);
    }

    /** Minerai au sol : pépites et ombre translucide, sur fond transparent (le sol reste visible). */
    private static BufferedImage ore(int size, Color[] r, Random rand){
        Canvas c = new Canvas(size);
        int n = 3 + rand.nextInt(2);
        for(int i = 0; i < n; i++){
            int rad = 2 + rand.nextInt(3), cx = 6 + rand.nextInt(size - 12), cy = 6 + rand.nextInt(size - 12);
            c.octagon(new Color(0, 0, 0, 70), cx + 1, cy + 2, rad + 1);
            pebble(c, cx, cy, rad, r);
        }
        c.outline();
        return c.img;
    }

    // ================= Blocs =================

    /** Plaque biseautée de la couleur du matériau, motif octogonal concentrique. */
    private static BufferedImage wall(int size, Color[] r){
        Canvas c = new Canvas(size);
        Polygon body = Canvas.chamfer(0, 0, size, size, size / 10);
        c.fill(body, r[2]);
        c.bevel(body, r[4], r[1], 2);
        int m = size / 2;
        if(size <= 32){
            Polygon inset = Canvas.chamfer(7, 7, size - 14, size - 14, 5);
            c.fill(inset, r[1]);
            Polygon plate = Canvas.chamfer(9, 9, size - 18, size - 18, 4);
            c.fill(plate, r[3]);
            c.bevel(plate, r[4], r[2], 1);
            c.octagon(r[2], m, m, 3);
        }else{
            int[] rings = {26, 22, 15, 10, 5};
            Color[] tones = {r[1], r[3], r[1], r[4], r[2]};
            for(int i = 0; i < rings.length; i++) c.octagon(tones[i], m, m, rings[i]);
            for(int[] q : new int[][]{{4, 4}, {size - 12, 4}, {4, size - 12}, {size - 12, size - 12}}){
                Polygon p = Canvas.chamfer(q[0], q[1], 8, 8, 2);
                c.fill(p, r[3]);
                c.bevel(p, r[4], r[1], 1);
            }
        }
        c.outline();
        return c.img;
    }

    /** Châssis métallique biseauté, équerres de couleur aux coins, creusets octogonaux incandescents. */
    private static BufferedImage crafter(int size, Color[] r, int crucibles){
        Canvas c = frame(size);
        cornerBrackets(c, size, r);
        int m = size / 2;
        grooves(c, size);
        if(crucibles == 1){
            diagonals(c, size, r);
            crucible(c, m, m, 14, r);
            for(int[] v : new int[][]{{m, 7}, {m, size - 8}, {7, m}, {size - 8, m}}) c.octagon(M[0], v[0], v[1], 2);
        }else{
            for(int y : new int[]{10, size - 14}){
                c.rect(r[1], 16, y, size - 32, 4);
                c.rect(r[3], 16, y, size - 32, 1);
                for(int x = 18; x < size - 18; x += 6) c.rect(r[0], x, y + 2, 3, 1);
            }
            crucible(c, m - 12, m, 10, r);
            crucible(c, m + 12, m, 10, r);
            c.rect(M[0], m - 3, m - 3, 6, 6);
            c.rect(r[2], m - 2, m - 2, 4, 4);
            c.rect(r[4], m - 2, m - 2, 4, 1);
        }
        c.outline();
        return c.img;
    }

    /** Bassin circulaire d'eau avec vaguelettes, entouré d'un anneau de couleur. */
    private static BufferedImage washer(int size, Color[] r){
        Canvas c = frame(size);
        cornerBrackets(c, size, ramp(NICKEL));
        int m = size / 2;
        c.octagon(M[0], m, m, 21);
        c.octagon(M[3], m, m, 19);
        c.octagon(M[1], m, m, 17);
        c.circle(r[1], m, m, 15);
        c.circle(r[2], m - 1, m - 1, 13);
        for(int k = 0; k < 3; k++){
            int y = m - 7 + k * 6;
            c.rect(r[4], m - 8 + k * 2, y, 6, 1);
            c.rect(r[3], m + 1 - k, y + 2, 5, 1);
        }
        c.octagon(M[2], m, m, 4);
        c.octagon(M[4], m - 1, m - 1, 2);
        c.outline();
        return c.img;
    }

    private static BufferedImage drillBase(int size, Color[] r){
        Canvas c = frame(size);
        cornerBrackets(c, size, r);
        int m = size / 2;
        c.octagon(M[0], m, m, 22);
        c.octagon(M[2], m, m, 20);
        c.octagon(M[0], m, m, 17);
        for(int i = 0; i < 8; i++){
            double a = i * Math.PI / 4;
            c.octagon(M[3], m + (int)Math.round(Math.cos(a) * 19), m + (int)Math.round(Math.sin(a) * 19), 1);
        }
        c.outline();
        return c.img;
    }

    /** Rotor à quatre pales facettées (moitié claire, moitié sombre), tourné par le jeu. */
    private static BufferedImage drillRotator(int size){
        Canvas c = new Canvas(size);
        int m = size / 2;
        for(int i = 0; i < 4; i++){
            AffineTransform t = AffineTransform.getRotateInstance(i * Math.PI / 2, m, m);
            c.poly(t, M[4], m - 5, m, m - 8, m - 15, m - 4, m - 21, m, m - 21, m, m);
            c.poly(t, M[2], m, m, m, m - 21, m + 4, m - 21, m + 8, m - 15, m + 5, m);
            c.poly(t, M[5], m - 4, m - 21, m, m - 21, m, m - 19, m - 3, m - 19);
        }
        Polygon hub = Canvas.chamfer(m - 7, m - 7, 14, 14, 4);
        c.fill(hub, M[3]);
        c.bevel(hub, M[5], M[1], 1);
        c.outline();
        return c.img;
    }

    private static BufferedImage drillTop(int size, Color[] r){
        Canvas c = new Canvas(size);
        int m = size / 2;
        Polygon cap = Canvas.chamfer(m - 5, m - 5, 10, 10, 3);
        c.fill(cap, r[2]);
        c.bevel(cap, r[4], r[1], 1);
        c.set(m - 1, m - 1, r[5]);
        c.outline();
        return c.img;
    }

    /**
     * Convoyeur vers la droite, forme selon le raccordement (géométrie du vanilla) :
     * 0 droit (rails haut et bas), 1 virage (entrée par le haut : rails à gauche et en bas),
     * 2 entrée latérale par le bas (rail en haut), 3 jonction (entrées des deux côtés et de l'arrière),
     * 4 deux entrées latérales sans arrière (rail à gauche). Les chevrons avancent de 2 px par image.
     */
    private static BufferedImage conveyor(int size, Color[] r, int shape, int frame, boolean armored){
        Canvas c = new Canvas(size);
        c.rect(M[0], 0, 0, size, size);
        Color belt = armored ? r[1] : r[1], beltLight = r[3];
        if(shape == 1){
            // Virage : barres radiales autour du coin haut-droit ; elles tournent du haut vers la droite.
            double step = Math.PI / 2 / 4;
            for(int k = -1; k < 5; k++){
                double a = Math.PI + k * step - frame * step / 4;
                if(a < Math.PI * 0.98 || a > Math.PI * 1.52) continue;
                for(int rad = 11; rad <= 21; rad++){
                    int x = size + (int)Math.round(Math.cos(a) * rad), y = (int)Math.round(-Math.sin(a) * rad);
                    int x2 = size + (int)Math.round(Math.cos(a + 0.12) * rad), y2 = (int)Math.round(-Math.sin(a + 0.12) * rad);
                    c.set(x, y, beltLight);
                    c.set(x2, y2, belt);
                    c.set(x2, y2 + 1, belt);
                }
            }
        }else{
            for(int k = -1; k < 5; k++){
                int x = k * 8 + frame * 2;
                c.poly(belt, x, 8, x + 3, 8, x + 7, 16, x + 3, 24, x, 24, x + 4, 16);
                c.poly(beltLight, x, 8, x + 3, 8, x + 7, 16, x + 4, 16);
            }
        }
        boolean top = shape == 0 || shape == 2, bottom = shape == 0 || shape == 1, left = shape == 1 || shape == 4;
        Color rail = armored ? r[2] : M[2], railLight = armored ? r[4] : M[4], railDark = armored ? r[0] : M[1];
        if(top) rail(c, 0, 1, size, 6, rail, railLight, railDark, true);
        if(bottom) rail(c, 0, size - 7, size, 6, rail, railLight, railDark, true);
        if(left) rail(c, 1, 0, 6, size, rail, railLight, railDark, false);
        // Coins : petits butoirs aux angles ouverts.
        for(int[] k : new int[][]{{0, 0}, {size - 6, 0}, {0, size - 6}, {size - 6, size - 6}}){
            boolean covered = (k[1] == 0 && top) || (k[1] != 0 && bottom) || (k[0] == 0 && left);
            if(!covered){
                Polygon nub = Canvas.chamfer(k[0], k[1], 6, 6, 2);
                c.fill(nub, rail);
                c.bevel(nub, railLight, railDark, 1);
            }
        }
        if(top) c.rect(OUTLINE, 0, 0, size, 1);
        if(bottom) c.rect(OUTLINE, 0, size - 1, size, 1);
        if(left) c.rect(OUTLINE, 0, 0, 1, size);
        return c.img;
    }

    private static void rail(Canvas c, int x, int y, int w, int h, Color base, Color light, Color dark, boolean horizontal){
        c.rect(base, x, y, w, h);
        if(horizontal){
            c.rect(light, x, y, w, 1);
            c.rect(dark, x, y + h - 1, w, 1);
            for(int i = x + 3; i < x + w; i += 8) c.rect(M[0], i, y + 2, 2, 2);
        }else{
            c.rect(light, x, y, 1, h);
            c.rect(dark, x + w - 1, y, 1, h);
            for(int i = y + 3; i < y + h; i += 8) c.rect(M[0], x + 2, i, 2, 2);
        }
    }

    /** Tourelle symétrique : corps facetté, plaques latérales colorées, double canon, noyau lumineux. */
    private static BufferedImage turret(int size, Color[] r){
        Canvas c = new Canvas(size);
        int m = size / 2;
        // double canon
        for(int bx : new int[]{m - 7, m + 3}){
            c.rect(M[3], bx, 4, 4, 22);
            c.rect(M[5], bx, 4, 1, 22);
            c.rect(M[1], bx + 3, 4, 1, 22);
            c.rect(M[1], bx - 1, 2, 6, 4);
            c.rect(M[0], bx, 2, 4, 1);
        }
        // corps
        Polygon body = Canvas.chamfer(m - 17, 20, 34, 34, 9);
        c.fill(body, M[2]);
        c.bevel(body, M[4], M[1], 2);
        // plaques latérales facettées
        c.poly(r[3], m - 23, 26, m - 15, 22, m - 15, 50, m - 23, 46);
        c.poly(r[1], m - 19, 24, m - 15, 22, m - 15, 50, m - 19, 48);
        c.poly(r[1], m + 23, 26, m + 15, 22, m + 15, 50, m + 23, 46);
        c.poly(r[3], m + 19, 24, m + 15, 22, m + 15, 50, m + 19, 48);
        // chevron d'accent
        c.poly(r[2], m - 12, 27, m, 22, m + 12, 27, m + 12, 30, m, 25, m - 12, 30);
        c.poly(r[4], m - 12, 27, m, 22, m + 12, 27, m, 23);
        // noyau
        c.octagon(M[0], m, 37, 8);
        c.octagon(r[1], m, 37, 6);
        c.octagon(r[3], m, 37, 4);
        c.octagon(r[5], m - 1, 36, 1);
        // évents arrière
        for(int i = 0; i < 3; i++) c.rect(M[0], m - 6 + i * 5, 49, 3, 2);
        c.outline();
        return c.img;
    }

    private static BufferedImage node(int size, Color[] r){
        Canvas c = new Canvas(size);
        int m = size / 2;
        Polygon base = Canvas.chamfer(4, 4, size - 8, size - 8, 6);
        c.fill(base, M[2]);
        c.bevel(base, M[4], M[1], 2);
        for(int[] n : new int[][]{{m, 6}, {m, size - 7}, {6, m}, {size - 7, m}}) c.octagon(M[0], n[0], n[1], 1);
        c.octagon(M[0], m, m, 8);
        c.octagon(r[1], m, m, 6);
        c.octagon(r[3], m, m, 4);
        c.octagon(r[5], m - 1, m - 1, 1);
        c.outline();
        return c.img;
    }

    /** Goutte de liquide facettée avec reflet. */
    private static BufferedImage liquid(int size, Color[] r){
        Canvas c = new Canvas(size);
        int m = size / 2;
        c.poly(r[3], m, 3, m - 9, 17, m - 10, 21, m, 21);
        c.poly(r[1], m, 3, m, 21, m + 10, 21, m + 9, 17);
        c.circle(r[1], m + 1, 21, 10);
        c.circle(r[2], m, 20, 9);
        c.circle(r[3], m - 2, 18, 6);
        c.rect(r[5], m - 6, 15, 2, 4);
        c.set(m - 4, 13, r[5]);
        c.outline();
        return c.img;
    }

    /** Presse : deux plaques-pistons colorées qui se font face, fente centrale incandescente. */
    private static BufferedImage press(int size, Color[] r){
        Canvas c = frame(size);
        cornerBrackets(c, size, r);
        int m = size / 2;
        for(int dir : new int[]{-1, 1}){
            int y = dir < 0 ? 12 : m + 4;
            Polygon plate = Canvas.chamfer(13, y, size - 26, m - 16, 3);
            c.fill(plate, r[2]);
            c.bevel(plate, r[4], r[1], 2);
            for(int x = 18; x < size - 18; x += 7) c.rect(r[1], x, y + 4, 4, m - 24);
        }
        c.rect(M[0], 12, m - 3, size - 24, 6);
        c.rect(hex("ff8a3d"), 14, m - 1, size - 28, 2);
        c.rect(hex("ffd9a0"), 20, m - 1, size - 40, 1);
        c.outline();
        return c.img;
    }

    /** Cuve ronde de liquide, conduites aux quatre côtés ; version cryo : givre et serpentin. */
    private static BufferedImage tank(int size, Color[] r, boolean cryo){
        Canvas c = frame(size);
        int m = size / 2;
        for(int[] p : new int[][]{{m - 3, 5, 6, 10}, {m - 3, size - 15, 6, 10}, {5, m - 3, 10, 6}, {size - 15, m - 3, 10, 6}}){
            c.rect(M[3], p[0], p[1], p[2], p[3]);
            c.rect(M[1], p[0] + 1, p[1] + 1, p[2] - 2, p[3] - 2);
        }
        c.circle(M[0], m, m, 19);
        c.circle(M[3], m, m, 17);
        c.circle(r[1], m, m, 15);
        c.circle(r[2], m - 1, m - 1, 12);
        c.circle(r[3], m - 4, m - 4, 6);
        c.rect(r[5], m - 9, m - 7, 3, 2);
        if(cryo){
            for(int i = 0; i < 3; i++){
                int y = m - 8 + i * 8;
                c.rect(M[4], m - 10, y, 20, 2);
                c.rect(M[2], m - 10, y + 2, 20, 1);
            }
            for(int[] f : new int[][]{{m - 13, m - 3}, {m + 11, m + 5}, {m + 6, m - 12}, {m - 6, m + 12}}){
                c.set(f[0], f[1], Color.WHITE);
                c.set(f[0] + 1, f[1], r[5]);
            }
        }else{
            c.octagon(M[2], m, m, 3);
            c.rect(M[4], m - 1, m - 10, 2, 20);
        }
        c.outline();
        return c.img;
    }

    /** Bassin rectangulaire de saumure, deux électrodes et arcs électriques. */
    private static BufferedImage electrolyzer(int size, Color[] r){
        Canvas c = frame(size);
        cornerBrackets(c, size, ramp(CHROME));
        int m = size / 2;
        Polygon basin = Canvas.chamfer(13, 15, size - 26, size - 30, 4);
        c.fill(basin, M[0]);
        Polygon fluid = Canvas.chamfer(15, 17, size - 30, size - 34, 3);
        c.fill(fluid, r[1]);
        c.rect(r[3], 17, 19, size - 34, 2);
        Color[] cu = ramp(hex("d99d73")), cr = ramp(CHROME);
        for(int[] e : new int[][]{{m - 13, 0}, {m + 8, 1}}){
            Color[] t = e[1] == 0 ? cu : cr;
            Polygon rod = Canvas.chamfer(e[0], 10, 6, size - 20, 2);
            c.fill(rod, t[2]);
            c.bevel(rod, t[4], t[0], 1);
        }
        Color spark = hex("bfe9ff");
        for(int i = 0; i < 3; i++){
            int y = m - 8 + i * 8;
            c.line(m - 6, y, m - 2, y + 3, spark);
            c.line(m - 2, y + 3, m + 3, y, spark);
        }
        c.outline();
        return c.img;
    }

    /** Tourelle-fusil : bouche large à cinq canons, corps trapu facetté. */
    private static BufferedImage shotgun(int size, Color[] r){
        Canvas c = new Canvas(size);
        int m = size / 2;
        Polygon muzzle = Canvas.chamfer(m - 13, 3, 26, 20, 4);
        c.fill(muzzle, M[3]);
        c.bevel(muzzle, M[5], M[1], 2);
        for(int i = 0; i < 5; i++) c.octagon(M[0], m - 10 + i * 5, 8, 1);
        c.rect(r[2], m - 13, 16, 26, 3);
        c.rect(r[4], m - 13, 16, 26, 1);
        Polygon body = Canvas.chamfer(m - 18, 20, 36, 34, 10);
        c.fill(body, M[2]);
        c.bevel(body, M[4], M[1], 2);
        c.poly(r[3], m - 18, 30, m - 10, 24, m - 10, 48, m - 18, 44);
        c.poly(r[1], m + 18, 30, m + 10, 24, m + 10, 48, m + 18, 44);
        c.octagon(M[0], m, 37, 7);
        c.octagon(r[2], m, 37, 5);
        c.octagon(r[4], m - 1, 36, 2);
        c.outline();
        return c.img;
    }

    /** Lance-liquide : réservoir rond sous une buse évasée. */
    private static BufferedImage nozzle(int size, Color[] r){
        Canvas c = new Canvas(size);
        int m = size / 2;
        c.poly(M[3], m - 4, 22, m - 8, 3, m + 8, 3, m + 4, 22);
        c.poly(M[5], m - 4, 22, m - 8, 3, m - 5, 3, m - 2, 22);
        c.rect(r[3], m - 7, 5, 14, 2);
        Polygon body = Canvas.chamfer(m - 17, 20, 34, 34, 10);
        c.fill(body, M[2]);
        c.bevel(body, M[4], M[1], 2);
        c.circle(M[0], m, 38, 11);
        c.circle(r[1], m, 38, 9);
        c.circle(r[2], m - 1, 37, 7);
        c.circle(r[4], m - 3, 35, 3);
        c.outline();
        return c.img;
    }

    /** Caisse de stockage : panneaux renforcés de sangles en croix et coins chanfreinés. */
    private static BufferedImage container(int size, Color[] r){
        Canvas c = new Canvas(size);
        Polygon body = Canvas.chamfer(0, 0, size, size, 7);
        c.fill(body, r[2]);
        c.bevel(body, r[4], r[0], 3);
        Polygon lid = Canvas.chamfer(8, 8, size - 16, size - 16, 5);
        c.fill(lid, r[1]);
        Polygon inner = Canvas.chamfer(10, 10, size - 20, size - 20, 4);
        c.fill(inner, r[3]);
        c.bevel(inner, r[4], r[2], 1);
        int m = size / 2;
        for(int i = 0; i < 2; i++){
            AffineTransform t = AffineTransform.getRotateInstance(i * Math.PI / 2, m, m);
            c.poly(t, M[2], 10, m - 3, size - 10, m - 3, size - 10, m + 3, 10, m + 3);
            c.poly(t, M[4], 10, m - 3, size - 10, m - 3, size - 10, m - 2, 10, m - 2);
        }
        c.octagon(M[0], m, m, 5);
        c.octagon(M[3], m, m, 3);
        c.outline();
        return c.img;
    }

    /** Rouleau de fibre de carbone : feuille tressée en losanges, bord enroulé. */
    private static BufferedImage fiber(int size, Color[] r){
        Canvas c = new Canvas(size);
        Polygon sheet = Canvas.chamfer(5, 7, 22, 18, 3);
        c.fill(sheet, r[1]);
        for(int y = 8; y < 25; y++){
            for(int x = 6; x < 27; x++){
                if(sheet.contains(x + 0.5, y + 0.5) && ((x + y) % 4 == 0 || (x - y + 40) % 4 == 0)) c.set(x, y, r[3]);
            }
        }
        c.bevel(sheet, r[4], r[0], 1);
        c.circle(r[2], 25, 16, 5);
        c.circle(r[0], 25, 16, 2);
        c.rect(r[4], 23, 11, 2, 1);
        c.outline();
        return c.img;
    }

    /** Tisseuse : deux bobines et les fils tendus entre elles, sur châssis. */
    private static BufferedImage weaver(int size, Color[] r){
        Canvas c = frame(size);
        cornerBrackets(c, size, ramp(DURALUMIN));
        int m = size / 2;
        for(int i = 0; i < 6; i++) c.rect(i % 2 == 0 ? r[3] : r[2], 18, m - 9 + i * 3, size - 36, 1);
        for(int x : new int[]{16, size - 16}){
            c.circle(M[0], x, m, 9);
            c.circle(r[1], x, m, 7);
            c.circle(r[3], x - 1, m - 1, 4);
            c.octagon(M[3], x, m, 2);
        }
        c.rect(M[0], m - 2, 12, 4, size - 24);
        c.rect(ramp(ACID)[3], m - 1, 14, 2, size - 28);
        c.outline();
        return c.img;
    }

    /** Résonateur 3x3 : chambre octogonale avec un grand cristal, quatre émetteurs reliés par des rayons. */
    private static BufferedImage resonator(int size, Color[] r){
        Canvas c = frame(size);
        cornerBrackets(c, size, r);
        grooves(c, size);
        int m = size / 2;
        c.octagon(M[0], m, m, 30);
        c.octagon(M[3], m, m, 28);
        c.octagon(M[0], m, m, 25);
        for(int i = 0; i < 4; i++){
            AffineTransform t = AffineTransform.getRotateInstance(i * Math.PI / 2, m, m);
            c.poly(t, r[3], m - 1, m - 24, m + 1, m - 24, m + 1, m - 10, m - 1, m - 10);
        }
        for(int[] e : new int[][]{{m, 14}, {m, size - 15}, {14, m}, {size - 15, m}}){
            c.octagon(M[0], e[0], e[1], 5);
            c.octagon(r[2], e[0], e[1], 3);
            c.octagon(r[5], e[0], e[1], 1);
        }
        // grand cristal central, mêmes facettes que l'objet
        int bx = m, by = m + 12, h = 26, w = 14;
        c.poly(r[3], bx, by - h, bx - w / 2, by - h * 2 / 3, bx - w / 2, by, bx, by);
        c.poly(r[1], bx, by - h, bx, by, bx + w / 2, by, bx + w / 2, by - h * 2 / 3);
        c.poly(r[4], bx, by - h, bx - w / 2, by - h * 2 / 3, bx - w / 4, by - h * 2 / 3 + 2, bx, by - h * 2 / 3 - 1);
        c.set(bx - 1, by - h + 4, r[5]);
        c.outline();
        return c.img;
    }

    /** Socle de pont (et extrémité, plus petite) : plaque chanfreinée et anneau de couleur. */
    private static BufferedImage bridgeBase(int size, Color[] r, int ring){
        Canvas c = new Canvas(size);
        int m = size / 2;
        Polygon pad = Canvas.chamfer(m - ring - 2, m - ring - 2, 2 * ring + 4, 2 * ring + 4, ring / 2);
        c.fill(pad, M[2]);
        c.bevel(pad, M[4], M[1], 2);
        c.octagon(M[0], m, m, ring - 3);
        c.octagon(r[2], m, m, ring - 5);
        c.octagon(r[4], m - 1, m - 1, Math.max(1, ring - 9));
        c.outline();
        return c.img;
    }

    /** Poutre du pont, horizontale (le jeu l'étire et la tourne entre les deux socles). */
    private static BufferedImage bridgeBeam(int size, Color[] r){
        Canvas c = new Canvas(size);
        int m = size / 2;
        c.rect(M[1], 0, m - 5, size, 10);
        c.rect(r[2], 0, m - 3, size, 6);
        c.rect(r[4], 0, m - 3, size, 1);
        c.rect(r[0], 0, m + 2, size, 1);
        for(int x = 2; x < size; x += 8) c.rect(M[0], x, m - 1, 3, 2);
        return c.img;
    }

    private static BufferedImage bridgeArrow(int size, Color[] r){
        Canvas c = new Canvas(size);
        int m = size / 2;
        c.poly(r[4], m - 4, m - 6, m + 4, m, m - 4, m + 6, m - 2, m);
        c.outline();
        return c.img;
    }

    /** Canon électrique 3x3 : deux rails parallèles sur toute la longueur, condensateurs lumineux, corps blindé. */
    private static BufferedImage railgun(int size, Color[] r){
        Canvas c = new Canvas(size);
        int m = size / 2;
        for(int dir : new int[]{-1, 1}){
            int x = m + dir * 7 - (dir < 0 ? 4 : 0);
            c.rect(M[3], x, 2, 4, m + 10);
            c.rect(dir < 0 ? M[5] : M[1], dir < 0 ? x : x + 3, 2, 1, m + 10);
        }
        c.rect(M[0], m - 3, 4, 6, m + 6);
        for(int y = 8; y < m + 6; y += 6) c.rect(r[3], m - 2, y, 4, 2);
        Polygon body = Canvas.chamfer(m - 26, m - 4, 52, 46, 14);
        c.fill(body, M[2]);
        c.bevel(body, M[4], M[1], 2);
        for(int dir : new int[]{-1, 1}){
            for(int i = 0; i < 3; i++){
                int cx = m + dir * 18, cy = m + 4 + i * 11;
                c.octagon(M[0], cx, cy, 4);
                c.octagon(r[2], cx, cy, 3);
                c.octagon(r[5], cx - 1, cy - 1, 1);
            }
        }
        c.octagon(M[0], m, m + 16, 10);
        c.octagon(r[1], m, m + 16, 8);
        c.octagon(r[3], m, m + 16, 5);
        c.octagon(r[5], m - 1, m + 15, 2);
        c.outline();
        return c.img;
    }

    /** Navire vu de dessus, proue vers le haut : coque facettée, pont métallique, bande de couleur. */
    private static BufferedImage naval(int size, Color[] r){
        Canvas c = new Canvas(size);
        int m = size / 2;
        float k = size / 40f;
        int w = Math.round(11 * k), bow = Math.round(3 * k), stern = size - Math.round(4 * k);
        c.poly(M[3], m, bow, m - w, bow + Math.round(12 * k), m - w, stern - Math.round(3 * k), m, stern);
        c.poly(M[1], m, bow, m, stern, m + w, stern - Math.round(3 * k), m + w, bow + Math.round(12 * k));
        int dw = Math.round(7 * k);
        Polygon deck = Canvas.chamfer(m - dw, bow + Math.round(12 * k), 2 * dw, stern - bow - Math.round(18 * k), Math.round(3 * k));
        c.fill(deck, M[2]);
        c.bevel(deck, M[4], M[0], 1);
        c.rect(r[2], m - w + 1, bow + Math.round(14 * k), Math.round(2 * k), Math.round(14 * k));
        c.rect(r[1], m + w - Math.round(2 * k), bow + Math.round(14 * k), Math.round(2 * k), Math.round(14 * k));
        c.octagon(M[0], m, m + Math.round(6 * k), Math.round(3 * k));
        c.octagon(r[3], m, m + Math.round(6 * k), Math.round(2 * k));
        if(k > 1.2f){
            // Navires plus grands : passerelle et seconde tourelle à l'avant.
            Polygon bridge = Canvas.chamfer(m - Math.round(4 * k), m - Math.round(3 * k), Math.round(8 * k), Math.round(5 * k), 2);
            c.fill(bridge, M[4]);
            c.bevel(bridge, M[5], M[2], 1);
            c.octagon(M[0], m, bow + Math.round(15 * k), Math.round(2.5f * k));
            c.octagon(r[3], m, bow + Math.round(15 * k), Math.round(1.5f * k));
        }
        if(k > 1.5f){
            for(int dir : new int[]{-1, 1}) c.rect(r[4], m + dir * Math.round(9 * k) - 1, stern - Math.round(10 * k), 2, Math.round(6 * k));
        }
        return c.img;
    }

    /** Châssis commun aux blocs de production : cadre biseauté, cuvette intérieure, boulons octogonaux. */
    private static Canvas frame(int size){
        Canvas c = new Canvas(size);
        Polygon outer = Canvas.chamfer(0, 0, size, size, 7);
        c.fill(outer, M[2]);
        c.bevel(outer, M[4], M[1], 2);
        Polygon recess = Canvas.chamfer(5, 5, size - 10, size - 10, 6);
        c.fill(recess, M[1]);
        Polygon plate = Canvas.chamfer(7, 7, size - 14, size - 14, 5);
        c.fill(plate, M[2]);
        c.bevel(plate, M[3], M[1], 1);
        return c;
    }

    /** Bandes de couleur en X, des coins vers le centre (motif des fours modernes). */
    private static void diagonals(Canvas c, int size, Color[] r){
        int m = size / 2;
        for(int i = 0; i < 4; i++){
            AffineTransform t = AffineTransform.getRotateInstance(i * Math.PI / 2, m, m);
            c.poly(t, r[1], 12, 15, 15, 12, m - 9, m - 12, m - 12, m - 9);
            c.poly(t, r[3], 12, 15, 15, 12, 16, 13, 13, 16);
            c.poly(t, r[2], 13, 16, 16, 13, m - 10, m - 13, m - 13, m - 10);
        }
    }

    /** Rainures d'usinage : anneau chanfreiné sombre et entailles sur les bords de la plaque. */
    private static void grooves(Canvas c, int size){
        Polygon ring = Canvas.chamfer(11, 11, size - 22, size - 22, 7);
        Polygon inner = Canvas.chamfer(12, 12, size - 24, size - 24, 7);
        for(int y = 0; y < size; y++){
            for(int x = 0; x < size; x++){
                if(ring.contains(x + 0.5, y + 0.5) && !inner.contains(x + 0.5, y + 0.5)) c.set(x, y, M[1]);
            }
        }
        int m = size / 2;
        for(int k : new int[]{m - 6, m + 4}){
            c.rect(M[1], k, 8, 2, 3);
            c.rect(M[1], k, size - 11, 2, 3);
            c.rect(M[1], 8, k, 3, 2);
            c.rect(M[1], size - 11, k, 3, 2);
        }
    }

    /** Équerres de couleur aux quatre coins, symétriques. */
    private static void cornerBrackets(Canvas c, int size, Color[] r){
        int s = size - 1;
        int[][] corners = {{0, 0, 1, 1}, {s, 0, -1, 1}, {0, s, 1, -1}, {s, s, -1, -1}};
        for(int[] k : corners){
            int x = k[0], y = k[1], dx = k[2], dy = k[3];
            Color light = (dx > 0 && dy > 0) ? r[4] : r[3], dark = (dx < 0 && dy < 0) ? r[1] : r[2];
            c.poly(dark, x + dx * 9, y + dy * 9, x + dx * 18, y + dy * 9, x + dx * 18, y + dy * 12, x + dx * 12, y + dy * 12,
                x + dx * 12, y + dy * 18, x + dx * 9, y + dy * 18);
            c.poly(light, x + dx * 9, y + dy * 9, x + dx * 18, y + dy * 9, x + dx * 18, y + dy * 10, x + dx * 10, y + dy * 10,
                x + dx * 10, y + dy * 18, x + dx * 9, y + dy * 18);
        }
    }

    /** Creuset : anneau métallique, bain coloré, cœur incandescent et reflet. */
    private static void crucible(Canvas c, int cx, int cy, int rad, Color[] r){
        c.octagon(M[0], cx, cy, rad + 2);
        Polygon rim = Canvas.chamfer(cx - rad, cy - rad, 2 * rad + 1, 2 * rad + 1, Math.max(1, Math.round(rad * 0.42f)));
        c.fill(rim, M[3]);
        c.bevel(rim, M[5], M[1], 1);
        c.octagon(M[1], cx, cy, rad - 2);
        c.octagon(r[1], cx, cy, rad - 4);
        c.octagon(r[3], cx, cy, rad - 6);
        c.octagon(r[5], cx, cy, Math.max(1, rad - 10));
        c.set(cx - rad / 2, cy - rad / 2, r[5]);
    }

    // ================= Unités (vues de dessus, vers le haut ; le jeu ajoute le contour) =================

    private static BufferedImage mechBody(int size, Color[] r){
        Canvas c = new Canvas(size);
        int m = size / 2;
        float k = size / 40f;
        int tw = Math.round(13 * k), th = Math.round(12 * k), sh = Math.round(19 * k);
        // épaulières facettées avec liseré coloré
        for(int dir : new int[]{-1, 1}){
            int x0 = m + dir * (tw - 2), x1 = m + dir * sh;
            c.poly(dir < 0 ? M[3] : M[1], x0, m - th + 2, x1, m - th + 5, x1, m + th - 4, x0, m + th - 1);
            c.poly(dir < 0 ? r[3] : r[1], x0, m - th + 2, x1, m - th + 5, x1, m - th + 8, x0, m - th + 5);
            c.rect(M[0], Math.min(x0, x1) + 2, m + 2, Math.abs(x1 - x0) - 3, 1);
        }
        // torse
        Polygon torso = Canvas.chamfer(m - tw, m - th, 2 * tw, 2 * th, Math.round(6 * k));
        c.fill(torso, M[2]);
        c.bevel(torso, M[4], M[0], 2);
        // plastron coloré à facettes
        int top = m - th - Math.round(3 * k);
        c.poly(r[3], m, top, m - tw + 3, m - th + 4, m - 6, m + 2, m, m);
        c.poly(r[1], m, top, m, m, m + 6, m + 2, m + tw - 3, m - th + 4);
        c.poly(r[4], m, top, m - tw + 3, m - th + 4, m - tw + 5, m - th + 4, m, top + 2);
        // visière
        c.rect(M[0], m - 4, m - th + 2, 8, 2);
        c.rect(r[5], m - 3, m - th + 2, 6, 1);
        // noyau et évents
        c.octagon(M[0], m, m + 5, Math.round(4 * k));
        c.octagon(r[3], m, m + 5, Math.round(3 * k) - 1);
        c.set(m - 1, m + 4, r[5]);
        for(int i = -1; i <= 1; i++) c.rect(M[1], m + i * 4 - 1, m + th - 4, 2, 2);
        if(k > 1.5f){
            // Grandes unités : plaques de blindage supplémentaires, feux d'épaule et bande de visière plus large.
            for(int dir : new int[]{-1, 1}){
                Polygon plate = Canvas.chamfer(m + dir * (tw - 4) - (dir < 0 ? 6 : 0), m - 3, 6, Math.round(8 * k), 2);
                c.fill(plate, M[3]);
                c.bevel(plate, M[5], M[1], 1);
                c.octagon(M[0], m + dir * (sh - 3), m - th + Math.round(9 * k), Math.round(1.5f * k));
                c.octagon(r[4], m + dir * (sh - 3), m - th + Math.round(9 * k), Math.max(1, Math.round(0.8f * k)));
            }
            c.rect(M[0], m - Math.round(6 * k), m - th + 2, Math.round(12 * k), 3);
            c.rect(r[5], m - Math.round(5 * k), m - th + 3, Math.round(10 * k), 1);
        }
        return c.img;
    }

    private static BufferedImage mechLeg(int size){
        Canvas c = new Canvas(size);
        int m = size / 2, off = size / 4 + 1, h = size * 2 / 5;
        for(int dir : new int[]{-1, 1}){
            Polygon foot = Canvas.chamfer(m + dir * off - 4, m - h / 2, 9, h, 3);
            c.fill(foot, M[2]);
            c.bevel(foot, M[4], M[0], 1);
            c.rect(M[1], m + dir * off - 2, m - 1, 5, 2);
        }
        return c.img;
    }

    private static BufferedImage mechBase(int size){
        Canvas c = new Canvas(size);
        int m = size / 2;
        Polygon hips = Canvas.chamfer(m - size / 4 - 2, m - 4, size / 2 + 4, 8, 3);
        c.fill(hips, M[2]);
        c.bevel(hips, M[3], M[0], 1);
        return c.img;
    }

    /** Aile delta en couches : ailes colorées facettées, fuselage métallique, verrière et réacteur. Proportionnel à la taille. */
    private static BufferedImage flyer(int size, Color[] r){
        Canvas c = new Canvas(size);
        int m = size / 2;
        float k = size / 40f;
        int w = Math.round(17 * k), tail = Math.round(12 * k), nose = Math.round(5 * k), mid = Math.round(6 * k);
        c.poly(r[3], m, nose, m - w, m + tail, m - w + Math.round(5 * k), m + tail + Math.round(2 * k), m, m + mid);
        c.poly(r[1], m, nose, m, m + mid, m + w - Math.round(5 * k), m + tail + Math.round(2 * k), m + w, m + tail);
        c.poly(r[4], m - w, m + tail, m - w + Math.round(5 * k), m + tail + Math.round(2 * k), m - w + Math.round(6 * k), m + tail - Math.round(1 * k));
        c.poly(r[2], m - Math.round(9 * k), m + Math.round(2 * k), m - Math.round(14 * k), m + Math.round(11 * k), m - Math.round(10 * k), m + Math.round(12 * k), m - Math.round(6 * k), m + Math.round(4 * k));
        c.poly(r[0], m + Math.round(9 * k), m + Math.round(2 * k), m + Math.round(14 * k), m + Math.round(11 * k), m + Math.round(10 * k), m + Math.round(12 * k), m + Math.round(6 * k), m + Math.round(4 * k));
        int fw = Math.max(3, Math.round(5 * k)), fl = Math.round(18 * k);
        c.poly(M[3], m, Math.round(2 * k), m - fw, m + Math.round(4 * k), m - fw + 1, m + fl - 2, m, m + fl);
        c.poly(M[1], m, Math.round(2 * k), m, m + fl, m + fw - 1, m + fl - 2, m + fw, m + Math.round(4 * k));
        if(k > 1.3f){
            // Grandes unités : plaques et lumières supplémentaires.
            for(int dir : new int[]{-1, 1}){
                c.octagon(M[0], m + dir * Math.round(10 * k), m + Math.round(8 * k), Math.round(2 * k));
                c.octagon(r[4], m + dir * Math.round(10 * k), m + Math.round(8 * k), Math.max(1, Math.round(k)));
            }
        }
        c.octagon(M[0], m, m - Math.round(3 * k), Math.round(3 * k));
        c.octagon(r[4], m, m - Math.round(3 * k), Math.round(2 * k));
        c.rect(r[5], m - Math.round(2 * k), m + fl - 2, Math.round(4 * k), Math.round(2 * k));
        return c.img;
    }

    /** Bombardier : ailes larges en flèche, soute centrale et deux réacteurs. Proportionnel à la taille. */
    private static BufferedImage bomber(int size, Color[] r){
        Canvas c = new Canvas(size);
        int m = size / 2;
        float k = size / 48f;
        int w = Math.round(22 * k), top = Math.round(8 * k);
        c.poly(r[3], m, top, m - w, m + Math.round(6 * k), m - w + Math.round(2 * k), m + Math.round(12 * k), m - Math.round(6 * k), m + Math.round(8 * k), m, m + Math.round(6 * k));
        c.poly(r[1], m, top, m, m + Math.round(6 * k), m + Math.round(6 * k), m + Math.round(8 * k), m + w - Math.round(2 * k), m + Math.round(12 * k), m + w, m + Math.round(6 * k));
        c.poly(r[4], m - w, m + Math.round(6 * k), m - w + Math.round(2 * k), m + Math.round(12 * k), m - w + Math.round(4 * k), m + Math.round(8 * k));
        Polygon hull = Canvas.chamfer(m - Math.round(6 * k), Math.round(4 * k), Math.round(12 * k), size - Math.round(10 * k), Math.round(4 * k));
        c.fill(hull, M[2]);
        c.bevel(hull, M[4], M[0], Math.max(1, Math.round(k)));
        c.rect(M[0], m - Math.round(3 * k), m - Math.round(2 * k), Math.round(6 * k), Math.round(10 * k));
        c.rect(r[2], m - Math.round(2 * k), m - Math.round(1 * k), Math.round(4 * k), Math.round(2 * k));
        c.rect(r[2], m - Math.round(2 * k), m + Math.round(4 * k), Math.round(4 * k), Math.round(2 * k));
        for(int dir : new int[]{-1, 1}){
            c.octagon(M[0], m + dir * Math.round(12 * k), m + Math.round(12 * k), Math.round(3 * k));
            c.octagon(r[4], m + dir * Math.round(12 * k), m + Math.round(12 * k), Math.max(1, Math.round(k)));
            if(k > 1.3f){
                c.octagon(M[0], m + dir * Math.round(18 * k), m + Math.round(10 * k), Math.round(2 * k));
                c.octagon(r[4], m + dir * Math.round(18 * k), m + Math.round(10 * k), Math.max(1, Math.round(k)));
            }
        }
        c.octagon(r[4], m, Math.round(9 * k), Math.round(2 * k));
        return c.img;
    }

    private static BufferedImage weapon(int size, Color[] r){
        Canvas c = new Canvas(size);
        int m = size / 2;
        c.rect(M[3], m - 2, 1, 4, size - 9);
        c.rect(M[5], m - 2, 1, 1, size - 9);
        c.rect(M[1], m + 1, 1, 1, size - 9);
        c.rect(M[0], m - 3, 0, 6, 3);
        Polygon breech = Canvas.chamfer(m - 4, size - 11, 8, 9, 2);
        c.fill(breech, r[2]);
        c.bevel(breech, r[4], r[1], 1);
        return c.img;
    }

    /** Icône du mod : graphique en courbe dans un cadre biseauté. */
    static BufferedImage icon(){
        Canvas c = new Canvas(64);
        Polygon frame = Canvas.chamfer(1, 1, 62, 62, 8);
        c.fill(frame, M[1]);
        c.bevel(frame, M[3], M[0], 2);
        Polygon screen = Canvas.chamfer(7, 7, 50, 50, 6);
        c.fill(screen, hex("1b1c24"));
        Color[] a = ramp(hex("ffc857"));
        int[] ys = {46, 41, 43, 33, 36, 24, 27, 14};
        for(int i = 0; i < ys.length - 1; i++) c.line(12 + i * 6, ys[i], 12 + (i + 1) * 6, ys[i + 1], a[3]);
        c.rect(M[2], 12, 50, 42, 2);
        c.outline();
        return c.img;
    }

    // ================= Couleurs =================

    /**
     * Rampe de 6 tons du plus sombre au plus clair. Les ombres glissent vers le bleu-violet et se saturent,
     * les lumières glissent vers le jaune et se désaturent : c'est ce qui donne des aplats « vivants ».
     */
    static Color[] ramp(Color base){
        float[] hsb = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);
        float[] bright = {0.38f, 0.6f, 0.82f, 1f, 1.14f, 1.28f};
        float[] sat = {0.12f, 0.08f, 0.03f, 0f, -0.12f, -0.28f};
        float[] hue = {0.05f, 0.03f, 0.01f, 0f, -0.02f, -0.04f};
        Color[] out = new Color[6];
        for(int i = 0; i < 6; i++){
            float h = shiftHue(hsb[0], i < 3 ? 0.68f : 0.14f, Math.abs(hue[i]));
            float s = clamp01(hsb[1] + sat[i] * (hsb[1] < 0.15f ? 0.3f : 1f));
            float b = clamp01(hsb[2] * bright[i] + (i >= 4 ? 0.04f * (i - 3) : 0f));
            out[i] = Color.getHSBColor(h, s, b);
        }
        // Rampe décalée d'un cran : r[2] = couleur de base, r[3..5] lumières, r[0..1] ombres.
        return new Color[]{out[0], out[1], out[3], out[4], out[5], mix(out[5], Color.WHITE, 0.45f)};
    }

    private static float shiftHue(float h, float target, float amount){
        float d = target - h;
        if(d > 0.5f) d -= 1f;
        if(d < -0.5f) d += 1f;
        float r = h + Math.signum(d) * Math.min(Math.abs(d), amount);
        return r < 0 ? r + 1 : r > 1 ? r - 1 : r;
    }

    static Color hex(String s){
        return new Color(Integer.parseInt(s, 16));
    }

    private static Color mix(Color a, Color b, float t){
        return new Color(clamp(a.getRed() + (b.getRed() - a.getRed()) * t), clamp(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
            clamp(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    private static int clamp(float v){
        return Math.max(0, Math.min(255, Math.round(v)));
    }

    private static float clamp01(float v){
        return Math.max(0f, Math.min(1f, v));
    }

    // ================= Toile de dessin pixel art =================

    /** Dessin sans anticrénelage : polygones pleins, octogones, biseaux et contour. */
    static final class Canvas{
        final BufferedImage img;
        final int size;

        Canvas(int size){
            this.size = size;
            img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        }

        /** Rectangle aux coins coupés à 45° (octogone si carré). */
        static Polygon chamfer(int x, int y, int w, int h, int k){
            return new Polygon(new int[]{x + k, x + w - k, x + w, x + w, x + w - k, x + k, x, x},
                new int[]{y, y, y + k, y + h - k, y + h, y + h, y + h - k, y + k}, 8);
        }

        void fill(Shape s, Color c){
            Rectangle b = s.getBounds();
            for(int y = Math.max(0, b.y); y < Math.min(size, b.y + b.height + 1); y++){
                for(int x = Math.max(0, b.x); x < Math.min(size, b.x + b.width + 1); x++){
                    if(s.contains(x + 0.5, y + 0.5)) set(x, y, c);
                }
            }
        }

        void poly(Color c, int... xy){
            poly(null, c, xy);
        }

        void poly(AffineTransform t, Color c, int... xy){
            Polygon p = new Polygon();
            for(int i = 0; i < xy.length; i += 2) p.addPoint(xy[i], xy[i + 1]);
            fill(t == null ? p : t.createTransformedShape(p), c);
        }

        void rect(Color c, int x, int y, int w, int h){
            for(int yy = y; yy < y + h; yy++) for(int xx = x; xx < x + w; xx++) set(xx, yy, c);
        }

        /** Octogone plein de « rayon » {@code r} centré sur (cx, cy). */
        void octagon(Color c, int cx, int cy, int r){
            if(r <= 0){
                set(cx, cy, c);
                return;
            }
            fill(chamfer(cx - r, cy - r, 2 * r + 1, 2 * r + 1, Math.max(1, Math.round(r * 0.42f))), c);
        }

        void circle(Color c, int cx, int cy, int r){
            for(int y = -r; y <= r; y++) for(int x = -r; x <= r; x++) if(x * x + y * y <= r * r + r) set(cx + x, cy + y, c);
        }

        /** Biseau : bord haut-gauche éclairé, bord bas-droit dans l'ombre, sur {@code w} pixels. */
        void bevel(Shape s, Color light, Color dark, int w){
            Rectangle b = s.getBounds();
            for(int y = b.y; y <= b.y + b.height; y++){
                for(int x = b.x; x <= b.x + b.width; x++){
                    if(!s.contains(x + 0.5, y + 0.5)) continue;
                    boolean lit = false, shadow = false;
                    for(int k = 1; k <= w; k++){
                        if(!s.contains(x - k + 0.5, y + 0.5) || !s.contains(x + 0.5, y - k + 0.5)) lit = true;
                        if(!s.contains(x + k + 0.5, y + 0.5) || !s.contains(x + 0.5, y + k + 0.5)) shadow = true;
                    }
                    if(lit && !shadow) set(x, y, light);
                    else if(shadow && !lit) set(x, y, dark);
                    else if(lit) set(x, y, light);
                }
            }
        }

        void line(int x0, int y0, int x1, int y1, Color c){
            int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
            for(int i = 0; i <= steps; i++){
                int x = x0 + (x1 - x0) * i / Math.max(1, steps), y = y0 + (y1 - y0) * i / Math.max(1, steps);
                rect(c, x, y, 2, 2);
            }
        }

        /** Contour sombre de 1 px autour de tout pixel opaque (les ombres translucides n'en reçoivent pas). */
        void outline(){
            boolean[][] solid = new boolean[size][size];
            for(int y = 0; y < size; y++) for(int x = 0; x < size; x++) solid[x][y] = (img.getRGB(x, y) >>> 24) > 200;
            for(int y = 0; y < size; y++){
                for(int x = 0; x < size; x++){
                    if(solid[x][y]) continue;
                    for(int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}){
                        int nx = x + d[0], ny = y + d[1];
                        if(nx >= 0 && ny >= 0 && nx < size && ny < size && solid[nx][ny]){
                            set(x, y, OUTLINE);
                            break;
                        }
                    }
                }
            }
        }

        void set(int x, int y, Color c){
            if(x < 0 || y < 0 || x >= size || y >= size) return;
            if(c.getAlpha() == 255){
                img.setRGB(x, y, c.getRGB());
                return;
            }
            // Mélange alpha simple pour les ombres translucides.
            int dst = img.getRGB(x, y), da = dst >>> 24;
            float a = c.getAlpha() / 255f;
            int outA = Math.min(255, Math.round(c.getAlpha() + da * (1 - a)));
            int rr = Math.round(c.getRed() * a + ((dst >> 16) & 255) * (1 - a));
            int gg = Math.round(c.getGreen() * a + ((dst >> 8) & 255) * (1 - a));
            int bb = Math.round(c.getBlue() * a + (dst & 255) * (1 - a));
            img.setRGB(x, y, outA << 24 | rr << 16 | gg << 8 | bb);
        }
    }
}
