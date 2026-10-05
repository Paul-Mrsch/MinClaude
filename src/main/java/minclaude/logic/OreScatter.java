package minclaude.logic;

/**
 * Répartition déterministe de gisements sur une carte : bruit de valeur à deux octaves, un champ par minerai.
 * Une case reçoit le premier minerai dont le bruit dépasse son seuil. Même graine = mêmes gisements.
 */
public final class OreScatter{
    /** @param threshold 0..1, plus haut = gisements plus rares ; @param scale taille des gisements en cases */
    public record OreSpec(String name, float threshold, float scale){}

    public interface Eligible{
        boolean test(int x, int y);
    }

    public interface Placer{
        void place(int x, int y, int oreIndex);
    }

    private OreScatter(){}

    /** @return nombre de cases recouvertes */
    public static int scatter(int width, int height, long seed, OreSpec[] ores, Eligible eligible, Placer placer){
        int placed = 0;
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                if(!eligible.test(x, y)) continue;
                for(int i = 0; i < ores.length; i++){
                    if(noise(seed + i * 7919L, x, y, ores[i].scale) > ores[i].threshold){
                        placer.place(x, y, i);
                        placed++;
                        break;
                    }
                }
            }
        }
        return placed;
    }

    /** Bruit lissé dans [0, 1]. */
    public static float noise(long seed, int x, int y, float scale){
        float a = value(seed, x / scale, y / scale);
        float b = value(seed ^ 0x5DEECE66DL, x / (scale / 2f), y / (scale / 2f));
        return a * 0.7f + b * 0.3f;
    }

    private static float value(long seed, float x, float y){
        int x0 = (int)Math.floor(x), y0 = (int)Math.floor(y);
        float fx = smooth(x - x0), fy = smooth(y - y0);
        float top = lerp(hash(seed, x0, y0), hash(seed, x0 + 1, y0), fx);
        float bottom = lerp(hash(seed, x0, y0 + 1), hash(seed, x0 + 1, y0 + 1), fx);
        return lerp(top, bottom, fy);
    }

    private static float hash(long seed, int x, int y){
        long h = seed * 0x9E3779B97F4A7C15L + x * 0xC2B2AE3D27D4EB4FL + y * 0x165667B19E3779F9L;
        h ^= h >>> 31;
        h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 29;
        return (h >>> 40) / (float)(1L << 24);
    }

    private static float smooth(float t){
        return t * t * (3f - 2f * t);
    }

    private static float lerp(float a, float b, float t){
        return a + (b - a) * t;
    }
}
