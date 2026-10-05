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
 * Usage : {@code ./gradlew generateSprites}. Chaque contenu du mod doit avoir une entrée dans {@link #SPECS}
 * (vérifié par les tests).
 *
 * <p>Style vanilla : contour sombre de 1 px, 3 à 4 nuances par couleur, lumière en haut à gauche.
 */
public final class SpriteGenerator{
    enum Kind{ ITEM, WALL, CRAFTER }

    record Spec(String folder, String name, Kind kind, int tiles, Color color){}

    /** Une ligne par sprite. Nom = nom interne du contenu, sans le préfixe du mod. */
    static final List<Spec> SPECS = List.of(
        new Spec("items", "cobalt", Kind.ITEM, 1, hex("3f6fd8")),
        new Spec("blocks", "cobalt-smelter", Kind.CRAFTER, 2, hex("3f6fd8")),
        new Spec("blocks", "cobalt-wall", Kind.WALL, 1, hex("3f6fd8")),
        new Spec("blocks", "cobalt-wall-large", Kind.WALL, 2, hex("3f6fd8"))
    );

    private static final Color OUTLINE = hex("2b2b33");
    private static final Color METAL_DARK = hex("4a4b53"), METAL = hex("6e7080"), METAL_LIGHT = hex("989aa4");

    public static void main(String[] args) throws IOException{
        File out = new File(args.length > 0 ? args[0] : "assets/sprites");
        for(Spec spec : SPECS){
            File f = new File(new File(out, spec.folder), spec.name + ".png");
            f.getParentFile().mkdirs();
            ImageIO.write(render(spec), "png", f);
            System.out.println("sprite " + f.getPath());
        }
        if(args.length > 1){
            ImageIO.write(icon(), "png", new File(args[1]));
            System.out.println("icon " + args[1]);
        }
    }

    static BufferedImage render(Spec spec){
        int size = 32 * spec.tiles;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Random rand = new Random(spec.name.hashCode());
        switch(spec.kind){
            case ITEM -> item(img, spec.color, rand);
            case WALL -> wall(img, spec.color, spec.tiles);
            case CRAFTER -> crafter(img, spec.color, spec.tiles);
        }
        return img;
    }

    /** Amas de cristaux : quelques losanges ombrés, puis un contour. */
    private static void item(BufferedImage img, Color base, Random rand){
        Color[] shades = shades(base);
        int[][] gems = {{16, 16, 9}, {10, 20, 6}, {22, 11, 6}, {21, 22, 5}};
        for(int[] g : gems){
            int cx = g[0] + rand.nextInt(3) - 1, cy = g[1] + rand.nextInt(3) - 1, r = g[2];
            for(int y = -r; y <= r; y++){
                for(int x = -r; x <= r; x++){
                    if(Math.abs(x) + Math.abs(y) > r) continue;
                    // Lumière en haut à gauche : face claire, face moyenne, face sombre.
                    Color c = x + y < -r / 2 ? shades[3] : x + y < r / 3 ? shades[2] : shades[1];
                    set(img, cx + x, cy + y, c);
                }
            }
        }
        outline(img);
    }

    /** Plaque biseautée de la couleur du matériau, rivets aux coins de chaque case. */
    private static void wall(BufferedImage img, Color base, int tiles){
        Color[] s = shades(base);
        int size = img.getWidth();
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
                // Rainure centrale qui donne du relief.
                fillRect(img, ox + 10, oy + 15, 12, 2, s[1]);
            }
        }
        outline(img);
    }

    /** Châssis métallique vanilla avec une cuve centrale de la couleur du produit. */
    private static void crafter(BufferedImage img, Color base, int tiles){
        Color[] s = shades(base);
        int size = img.getWidth();
        fillRect(img, 1, 1, size - 2, size - 2, METAL);
        fillRect(img, 1, 1, size - 2, 3, METAL_LIGHT);
        fillRect(img, 1, size - 4, size - 2, 3, METAL_DARK);
        fillRect(img, 1, 1, 3, size - 2, METAL_LIGHT);
        fillRect(img, size - 4, 1, 3, size - 2, METAL_DARK);
        int m = size / 4;
        fillRect(img, m - 2, m - 2, size - 2 * m + 4, size - 2 * m + 4, OUTLINE);
        fillRect(img, m, m, size - 2 * m, size - 2 * m, s[1]);
        fillRect(img, m + 2, m + 2, size - 2 * m - 4, size - 2 * m - 4, s[2]);
        fillRect(img, m + 4, m + 4, (size - 2 * m) / 3, 3, s[3]);
        for(int[] p : new int[][]{{6, 6}, {size - 8, 6}, {6, size - 8}, {size - 8, size - 8}}){
            fillRect(img, p[0], p[1], 3, 3, METAL_DARK);
            set(img, p[0], p[1], METAL_LIGHT);
        }
        outline(img);
    }

    /** Icône du mod : petit graphique en courbe sur fond sombre. */
    static BufferedImage icon(){
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        fillRect(img, 2, 2, 60, 60, hex("1f2029"));
        Color accent = hex("ffd37f");
        int[] ys = {48, 44, 46, 36, 38, 26, 30, 16};
        for(int i = 0; i < ys.length - 1; i++){
            line(img, 8 + i * 7, ys[i], 8 + (i + 1) * 7, ys[i + 1], accent);
        }
        fillRect(img, 8, 52, 50, 2, hex("6e7080"));
        outline(img);
        return img;
    }

    /** Quatre nuances : très sombre, sombre, base, claire. */
    static Color[] shades(Color base){
        return new Color[]{mul(base, 0.45f), mul(base, 0.7f), base, mix(base, Color.WHITE, 0.35f)};
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
