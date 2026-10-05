package minclaude.stats;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContrastTest{
    // Pal.darkestGray (fond des graphiques) ≈ #2b2b2b
    private static final float BG = 0x2b / 255f;

    private static float ratioOnBg(float[] c){
        return Contrast.ratio(Contrast.luminance(c[0], c[1], c[2]), Contrast.luminance(BG, BG, BG));
    }

    @Test
    void knownRatios(){
        assertEquals(21f, Contrast.ratio(Contrast.luminance(1, 1, 1), Contrast.luminance(0, 0, 0)), 0.01);
        assertEquals(1f, Contrast.ratio(0.3f, 0.3f), 1e-6);
    }

    @Test
    void coalBecomesReadable(){
        float c = 0x27 / 255f; // couleur du charbon dans le jeu
        float[] out = Contrast.readableOn(c, c, c, BG, BG, BG);
        assertTrue(ratioOnBg(out) >= Contrast.MIN_RATIO);
        assertTrue(ratioOnBg(out) < Contrast.MIN_RATIO + 0.5f, "juste assez éclairci, pas blanchi");
    }

    @Test
    void readableColorIsUnchanged(){
        float[] copper = {0xd9 / 255f, 0x9d / 255f, 0x73 / 255f};
        assertArrayEquals(copper, Contrast.readableOn(copper[0], copper[1], copper[2], BG, BG, BG));
    }

    @Test
    void hueIsKept(){
        float[] darkBlue = {0.05f, 0.05f, 0.3f};
        float[] out = Contrast.readableOn(darkBlue[0], darkBlue[1], darkBlue[2], BG, BG, BG);
        assertTrue(out[2] > out[0] && out[2] > out[1], "reste bleu");
        assertTrue(ratioOnBg(out) >= Contrast.MIN_RATIO);
    }
}
