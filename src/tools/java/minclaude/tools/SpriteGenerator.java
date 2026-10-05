package minclaude.tools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Génère les sprites pixel art du mod, de façon déterministe (même entrée = mêmes pixels).
 * Usage : {@code ./gradlew generateSprites}. Chaque contenu du mod doit avoir ses sprites (vérifié par ContentIT).
 *
 * <p>Style vanilla : contour sombre, 3 à 4 nuances par couleur, lumière en haut à gauche.
 * Orientation du jeu : convoyeurs vers la droite, tourelles, unités et armes vers le haut.
 */
public final class SpriteGenerator{
    enum Kind{ GEM, INGOT, ORE, WALL, CRAFTER, WASHER, DRILL, CONVEYOR, TURRET, NODE, MECH, FLYER, WEAPON }

    /** @param size taille en pixels (côté) */
    record Spec(String folder, String name, Kind kind, int size, Color color){}

    private static final Color COBALT = hex("3f6fd8"), NICKEL = hex("b9b48f"), ZINC = hex("9fb7c4"), BAUXITE = hex("b5653d"),
        ALUMINUM = hex("d6dde3"), BRASS = hex("e0b84f"), ALLY = hex("ffd37f"), ENEMY = hex("e05438");

    /** Une ligne par contenu. Nom = nom interne sans le préfixe du mod. */
    static final List<Spec> SPECS = List.of(
        new Spec("items", "cobalt", Kind.GEM, 32, COBALT),
        new Spec("items", "nickel", Kind.GEM, 32, NICKEL),
        new Spec("items", "zinc", Kind.GEM, 32, ZINC),
        new Spec("items", "bauxite", Kind.GEM, 32, BAUXITE),
        new Spec("items", "aluminum", Kind.INGOT, 32, ALUMINUM),
        new Spec("items", "brass", Kind.INGOT, 32, BRASS),

        new Spec("blocks/environment", "ore-cobalt", Kind.ORE, 32, COBALT),
        new Spec("blocks/environment", "ore-nickel", Kind.ORE, 32, NICKEL),
        new Spec("blocks/environment", "ore-zinc", Kind.ORE, 32, ZINC),
        new Spec("blocks/environment", "ore-bauxite", Kind.ORE, 32, BAUXITE),

        new Spec("blocks", "cobalt-smelter", Kind.CRAFTER, 64, COBALT),
        new Spec("blocks", "aluminum-smelter", Kind.CRAFTER, 64, ALUMINUM),
        new Spec("blocks", "brass-foundry", Kind.CRAFTER, 64, BRASS),
        new Spec("blocks", "ore-washer", Kind.WASHER, 64, hex("5f9de0")),
        new Spec("blocks", "percussion-drill", Kind.DRILL, 64, NICKEL),
        new Spec("blocks", "cobalt-wall", Kind.WALL, 32, COBALT),
        new Spec("blocks", "cobalt-wall-large", Kind.WALL, 64, COBALT),
        new Spec("blocks", "nickel-wall", Kind.WALL, 32, NICKEL),
        new Spec("blocks", "nickel-wall-large", Kind.WALL, 64, NICKEL),
        new Spec("blocks", "reinforced-conveyor", Kind.CONVEYOR, 32, ALUMINUM),
        new Spec("blocks", "rivet", Kind.TURRET, 64, NICKEL),
        new Spec("blocks", "aluminum-node", Kind.NODE, 32, ALUMINUM),

        new Spec("units", "warden", Kind.MECH, 40, ALLY),
        new Spec("units", "aid", Kind.FLYER, 40, hex("8ce6a0")),
        new Spec("units", "marauder", Kind.MECH, 48, ENEMY),
        new Spec("units", "wasp", Kind.FLYER, 40, ENEMY),
        new Spec("units/weapons", "warden-gun", Kind.WEAPON, 24, ALLY),
        new Spec("units/weapons", "marauder-cannon", Kind.WEAPON, 28, ENEMY)
    );

    private static final Color OUTLINE = hex("2b2b33");
    private static final Color METAL_DARK = hex("4a4b53"), METAL = hex("6e7080"), METAL_LIGHT = hex("989aa4");

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

    /** Nom de fichier (sans .png) -> image. Certains contenus demandent plusieurs régions. */
    static Map<String, BufferedImage> render(Spec spec){
        Map<String, BufferedImage> out = new LinkedHashMap<>();
        Random rand = new Random(spec.name.hashCode());
        Color[] s = shades(spec.color);
        switch(spec.kind){
            case GEM -> out.put(spec.name, gem(spec.size, s, rand));
            case INGOT -> out.put(spec.name, ingot(spec.size, s));
            case ORE -> {
                for(int v = 1; v <= 3; v++) out.put(spec.name + v, ore(spec.size, s, new Random(spec.name.hashCode() * 31L + v)));
            }
            case WALL -> out.put(spec.name, wall(spec.size, s));
            case CRAFTER -> out.put(spec.name, crafter(spec.size, s, false));
            case WASHER -> out.put(spec.name, crafter(spec.size, s, true));
            case DRILL -> {
                out.put(spec.name, drillBase(spec.size));
                out.put(spec.name + "-rotator", drillRotator(spec.size, s));
                out.put(spec.name + "-top", drillTop(spec.size, s));
            }
            case CONVEYOR -> {
                // 7 formes x 4 images d'animation, nommées comme le vanilla : name-forme-image.
                for(int shape = 0; shape < 7; shape++){
                    for(int frame = 0; frame < 4; frame++) out.put(spec.name + "-" + shape + "-" + frame, conveyor(spec.size, s, frame));
                }
                out.put(spec.name, conveyor(spec.size, s, 0));
            }
            case TURRET -> out.put(spec.name, turret(spec.size, s));
            case NODE -> out.put(spec.name, node(spec.size, s));
            case MECH -> {
                out.put(spec.name, mechBody(spec.size, s));
                out.put(spec.name + "-leg", mechLeg(spec.size));
                out.put(spec.name + "-base", mechBase(spec.size));
            }
            case FLYER -> out.put(spec.name, flyer(spec.size, s));
            case WEAPON -> out.put(spec.name, weapon(spec.size, s));
        }
        return out;
    }

    // ---- Ressources ----

    /** Amas de cristaux : quelques losanges ombrés. */
    private static BufferedImage gem(int size, Color[] s, Random rand){
        BufferedImage img = image(size);
        int[][] gems = {{16, 16, 9}, {10, 20, 6}, {22, 11, 6}, {21, 22, 5}};
        for(int[] g : gems){
            int cx = g[0] + rand.nextInt(3) - 1, cy = g[1] + rand.nextInt(3) - 1, r = g[2];
            for(int y = -r; y <= r; y++){
                for(int x = -r; x <= r; x++){
                    if(Math.abs(x) + Math.abs(y) > r) continue;
                    Color c = x + y < -r / 2 ? s[3] : x + y < r / 3 ? s[2] : s[1];
                    set(img, cx + x, cy + y, c);
                }
            }
        }
        outline(img);
        return img;
    }

    /** Lingot vu de trois quarts : dessus clair, face avant, flanc sombre. */
    private static BufferedImage ingot(int size, Color[] s){
        BufferedImage img = image(size);
        for(int y = 0; y < 8; y++) fillRect(img, 6 + (8 - y) / 2, 9 + y, 20 - (8 - y), 1, s[3]);
        fillRect(img, 6, 17, 20, 7, s[2]);
        fillRect(img, 6, 22, 20, 2, s[1]);
        fillRect(img, 25, 13, 2, 11, s[0]);
        fillRect(img, 10, 12, 6, 1, Color.WHITE);
        outline(img);
        return img;
    }

    /** Minerai au sol : cailloux colorés sur fond transparent (le sol reste visible). */
    private static BufferedImage ore(int size, Color[] s, Random rand){
        BufferedImage img = image(size);
        for(int i = 0; i < 5; i++){
            int cx = 5 + rand.nextInt(size - 10), cy = 5 + rand.nextInt(size - 10), r = 2 + rand.nextInt(3);
            for(int y = -r; y <= r; y++){
                for(int x = -r; x <= r; x++){
                    if(x * x + y * y > r * r) continue;
                    set(img, cx + x, cy + y, x + y < 0 ? s[3] : s[2]);
                }
            }
            set(img, cx + r / 2, cy + r / 2, s[1]);
        }
        outline(img);
        return img;
    }

    // ---- Blocs ----

    /** Plaque biseautée de la couleur du matériau, rivets aux coins de chaque case. */
    private static BufferedImage wall(int size, Color[] s){
        BufferedImage img = image(size);
        int tiles = size / 32;
        fillRect(img, 1, 1, size - 2, size - 2, s[2]);
        fillRect(img, 1, 1, size - 2, 2, s[3]);
        fillRect(img, 1, 1, 2, size - 2, s[3]);
        fillRect(img, 1, size - 3, size - 2, 2, s[0]);
        fillRect(img, size - 3, 1, 2, size - 2, s[0]);
        for(int ty = 0; ty < tiles; ty++){
            for(int tx = 0; tx < tiles; tx++){
                int ox = tx * 32, oy = ty * 32;
                for(int[] p : new int[][]{{6, 6}, {25, 6}, {6, 25}, {25, 25}}){
                    fillRect(img, ox + p[0], oy + p[1], 2, 2, s[1]);
                    set(img, ox + p[0], oy + p[1], s[3]);
                }
                fillRect(img, ox + 10, oy + 15, 12, 2, s[1]);
            }
        }
        outline(img);
        return img;
    }

    /** Châssis métallique vanilla. Fonderie : cuve carrée. Laveur : bassin rond. */
    private static BufferedImage crafter(int size, Color[] s, boolean round){
        BufferedImage img = image(size);
        frame(img, size);
        int m = size / 4;
        if(round){
            int c = size / 2, r = size / 2 - m + 2;
            disc(img, c, c, r + 2, OUTLINE);
            disc(img, c, c, r, s[1]);
            disc(img, c - 1, c - 1, r - 3, s[2]);
            disc(img, c - 4, c - 4, r / 3, s[3]);
        }else{
            fillRect(img, m - 2, m - 2, size - 2 * m + 4, size - 2 * m + 4, OUTLINE);
            fillRect(img, m, m, size - 2 * m, size - 2 * m, s[1]);
            fillRect(img, m + 2, m + 2, size - 2 * m - 4, size - 2 * m - 4, s[2]);
            fillRect(img, m + 4, m + 4, (size - 2 * m) / 3, 3, s[3]);
        }
        outline(img);
        return img;
    }

    private static BufferedImage drillBase(int size){
        BufferedImage img = image(size);
        frame(img, size);
        disc(img, size / 2, size / 2, size / 3, METAL_DARK);
        outline(img);
        return img;
    }

    /** Rotor à quatre pales, dessiné sur la base et tourné par le jeu. */
    private static BufferedImage drillRotator(int size, Color[] s){
        BufferedImage img = image(size);
        int c = size / 2, len = size / 3, w = size / 10;
        fillRect(img, c - w / 2, c - len, w, len * 2, METAL_LIGHT);
        fillRect(img, c - len, c - w / 2, len * 2, w, METAL_LIGHT);
        fillRect(img, c - w / 2 + 1, c - len + 1, w / 2, len * 2 - 2, s[3]);
        outline(img);
        return img;
    }

    private static BufferedImage drillTop(int size, Color[] s){
        BufferedImage img = image(size);
        int c = size / 2;
        disc(img, c, c, size / 7, s[1]);
        disc(img, c - 1, c - 1, size / 10, s[2]);
        outline(img);
        return img;
    }

    /** Tapis horizontal vers la droite ; les chevrons avancent de 2 px par image. */
    private static BufferedImage conveyor(int size, Color[] s, int frame){
        BufferedImage img = image(size);
        fillRect(img, 0, 4, size, size - 8, METAL_DARK);
        fillRect(img, 0, 4, size, 3, METAL_LIGHT);
        fillRect(img, 0, size - 7, size, 3, METAL);
        fillRect(img, 0, 8, size, size - 16, s[0]);
        for(int k = -1; k < 5; k++){
            int x0 = k * 8 + frame * 2;
            for(int i = 0; i < 4; i++){
                set(img, x0 + i, 10 + i, s[2]);
                set(img, x0 + i, size - 11 - i, s[2]);
                set(img, x0 + i + 1, 10 + i, s[3]);
                set(img, x0 + i + 1, size - 11 - i, s[3]);
            }
        }
        // Pas de contour à gauche/droite : les tronçons se raccordent.
        fillRect(img, 0, 3, size, 1, OUTLINE);
        fillRect(img, 0, size - 4, size, 1, OUTLINE);
        return img;
    }

    /** Tourelle vue de dessus, canon vers le haut ; la base vanilla « block-2 » est dessinée dessous par le jeu. */
    private static BufferedImage turret(int size, Color[] s){
        BufferedImage img = image(size);
        int c = size / 2;
        fillRect(img, c - 5, 4, 10, c, METAL);
        fillRect(img, c - 5, 4, 3, c, METAL_LIGHT);
        fillRect(img, c - 6, 2, 12, 4, METAL_DARK);
        disc(img, c, c + 6, size / 4, s[1]);
        disc(img, c - 1, c + 5, size / 4 - 3, s[2]);
        disc(img, c - 4, c + 2, 3, s[3]);
        outline(img);
        return img;
    }

    private static BufferedImage node(int size, Color[] s){
        BufferedImage img = image(size);
        fillRect(img, 6, 6, size - 12, size - 12, METAL);
        fillRect(img, 6, 6, size - 12, 2, METAL_LIGHT);
        disc(img, size / 2, size / 2, 6, s[1]);
        disc(img, size / 2 - 1, size / 2 - 1, 4, s[3]);
        outline(img);
        return img;
    }

    private static void frame(BufferedImage img, int size){
        fillRect(img, 1, 1, size - 2, size - 2, METAL);
        fillRect(img, 1, 1, size - 2, 3, METAL_LIGHT);
        fillRect(img, 1, size - 4, size - 2, 3, METAL_DARK);
        fillRect(img, 1, 1, 3, size - 2, METAL_LIGHT);
        fillRect(img, size - 4, 1, 3, size - 2, METAL_DARK);
        for(int[] p : new int[][]{{6, 6}, {size - 8, 6}, {6, size - 8}, {size - 8, size - 8}}){
            fillRect(img, p[0], p[1], 3, 3, METAL_DARK);
            set(img, p[0], p[1], METAL_LIGHT);
        }
    }

    // ---- Unités (vues de dessus, vers le haut ; le jeu ajoute le contour) ----

    private static BufferedImage mechBody(int size, Color[] s){
        BufferedImage img = image(size);
        int c = size / 2, r = size / 4;
        disc(img, c, c, r + 2, METAL_DARK);
        disc(img, c, c, r, METAL);
        fillRect(img, c - r / 2, c - r - 2, r, r / 2, s[2]);
        disc(img, c, c - 1, r / 2, s[2]);
        disc(img, c - 1, c - 2, r / 4, s[3]);
        return img;
    }

    private static BufferedImage mechLeg(int size){
        BufferedImage img = image(size);
        int c = size / 2, w = size / 6;
        fillRect(img, c - size / 4 - w / 2, c - size / 5, w, size * 2 / 5, METAL_DARK);
        fillRect(img, c + size / 4 - w / 2, c - size / 5, w, size * 2 / 5, METAL_DARK);
        return img;
    }

    private static BufferedImage mechBase(int size){
        BufferedImage img = image(size);
        int c = size / 2;
        fillRect(img, c - size / 4, c - 3, size / 2, 6, METAL);
        return img;
    }

    /** Aile delta pointant vers le haut. */
    private static BufferedImage flyer(int size, Color[] s){
        BufferedImage img = image(size);
        int c = size / 2;
        for(int y = 4; y < size - 6; y++){
            int half = (y - 4) * (size / 2 - 3) / (size - 10);
            for(int x = c - half; x <= c + half; x++) set(img, x, y, x < c ? s[2] : s[1]);
        }
        fillRect(img, c - 2, 6, 4, size - 14, METAL_LIGHT);
        disc(img, c, size / 2, 3, s[3]);
        return img;
    }

    private static BufferedImage weapon(int size, Color[] s){
        BufferedImage img = image(size);
        int c = size / 2;
        fillRect(img, c - 2, 2, 4, size - 8, METAL);
        fillRect(img, c - 2, 2, 1, size - 8, METAL_LIGHT);
        fillRect(img, c - 4, size - 9, 8, 7, s[1]);
        fillRect(img, c - 3, size - 8, 3, 3, s[3]);
        return img;
    }

    /** Icône du mod : petit graphique en courbe sur fond sombre. */
    static BufferedImage icon(){
        BufferedImage img = image(64);
        fillRect(img, 2, 2, 60, 60, hex("1f2029"));
        Color accent = hex("ffd37f");
        int[] ys = {48, 44, 46, 36, 38, 26, 30, 16};
        for(int i = 0; i < ys.length - 1; i++) line(img, 8 + i * 7, ys[i], 8 + (i + 1) * 7, ys[i + 1], accent);
        fillRect(img, 8, 52, 50, 2, hex("6e7080"));
        outline(img);
        return img;
    }

    // ---- Outils ----

    /** Quatre nuances : très sombre, sombre, base, claire. */
    static Color[] shades(Color base){
        return new Color[]{mul(base, 0.45f), mul(base, 0.7f), base, mix(base, Color.WHITE, 0.35f)};
    }

    private static BufferedImage image(int size){
        return new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
    }

    /** Ajoute un contour sombre autour de tous les pixels opaques. */
    static void outline(BufferedImage img){
        int w = img.getWidth(), h = img.getHeight();
        boolean[][] solid = new boolean[w][h];
        for(int y = 0; y < h; y++) for(int x = 0; x < w; x++) solid[x][y] = (img.getRGB(x, y) >>> 24) != 0;
        for(int y = 0; y < h; y++){
            for(int x = 0; x < w; x++){
                if(solid[x][y]) continue;
                boolean edge = false;
                for(int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}){
                    int nx = x + d[0], ny = y + d[1];
                    if(nx >= 0 && ny >= 0 && nx < w && ny < h && solid[nx][ny]) edge = true;
                }
                if(edge) set(img, x, y, OUTLINE);
            }
        }
    }

    private static void disc(BufferedImage img, int cx, int cy, int r, Color c){
        for(int y = -r; y <= r; y++) for(int x = -r; x <= r; x++) if(x * x + y * y <= r * r) set(img, cx + x, cy + y, c);
    }

    private static void line(BufferedImage img, int x0, int y0, int x1, int y1, Color c){
        int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
        for(int i = 0; i <= steps; i++){
            int x = x0 + (x1 - x0) * i / Math.max(1, steps), y = y0 + (y1 - y0) * i / Math.max(1, steps);
            fillRect(img, x, y, 2, 2, c);
        }
    }

    private static void fillRect(BufferedImage img, int x, int y, int w, int h, Color c){
        for(int yy = y; yy < y + h; yy++) for(int xx = x; xx < x + w; xx++) set(img, xx, yy, c);
    }

    private static void set(BufferedImage img, int x, int y, Color c){
        if(x >= 0 && y >= 0 && x < img.getWidth() && y < img.getHeight()) img.setRGB(x, y, c.getRGB());
    }

    static Color hex(String s){
        return new Color(Integer.parseInt(s, 16));
    }

    private static Color mul(Color c, float f){
        return new Color(clamp(c.getRed() * f), clamp(c.getGreen() * f), clamp(c.getBlue() * f));
    }

    private static Color mix(Color a, Color b, float t){
        return new Color(clamp(a.getRed() + (b.getRed() - a.getRed()) * t), clamp(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
            clamp(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    private static int clamp(float v){
        return Math.max(0, Math.min(255, Math.round(v)));
    }
}
