package minclaude.stats;

/**
 * Lisibilité des couleurs sur fond sombre (formules WCAG 2). Certaines ressources ont une couleur
 * presque noire (charbon : #272727) : tracée telle quelle sur le fond du graphique, la courbe disparaît.
 */
public final class Contrast{
    /** Contraste minimal visé pour une courbe ou un texte (WCAG AA : 4,5:1 pour un texte). */
    public static final float MIN_RATIO = 4.5f;

    private Contrast(){}

    /** Luminance relative d'une couleur (composantes 0..1, espace sRGB). */
    public static float luminance(float r, float g, float b){
        return 0.2126f * linear(r) + 0.7152f * linear(g) + 0.0722f * linear(b);
    }

    public static float ratio(float lumA, float lumB){
        float hi = Math.max(lumA, lumB), lo = Math.min(lumA, lumB);
        return (hi + 0.05f) / (lo + 0.05f);
    }

    /**
     * Éclaircit {@code rgb} vers le blanc, juste assez pour atteindre {@link #MIN_RATIO} sur le fond donné.
     * La teinte est conservée autant que possible ; une couleur déjà lisible est renvoyée inchangée.
     */
    public static float[] readableOn(float r, float g, float b, float bgR, float bgG, float bgB){
        float bg = luminance(bgR, bgG, bgB);
        if(ratio(luminance(r, g, b), bg) >= MIN_RATIO) return new float[]{r, g, b};
        // Recherche dichotomique de la plus petite part de blanc suffisante.
        float lo = 0f, hi = 1f;
        for(int i = 0; i < 20; i++){
            float t = (lo + hi) / 2f;
            if(ratio(luminance(mix(r, t), mix(g, t), mix(b, t)), bg) >= MIN_RATIO) hi = t;
            else lo = t;
        }
        return new float[]{mix(r, hi), mix(g, hi), mix(b, hi)};
    }

    private static float mix(float c, float t){
        return c + (1f - c) * t;
    }

    private static float linear(float c){
        return c <= 0.04045f ? c / 12.92f : (float)Math.pow((c + 0.055f) / 1.055f, 2.4f);
    }
}
