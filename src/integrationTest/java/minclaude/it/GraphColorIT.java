package minclaude.it;

import arc.graphics.Color;
import minclaude.stats.Contrast;
import minclaude.ui.LineGraph;
import mindustry.type.Item;
import org.junit.jupiter.api.*;

import static mindustry.Vars.content;
import static org.junit.jupiter.api.Assertions.*;

/** Toutes les ressources du jeu (vanilla + mod) ont une courbe lisible dans le dashboard. Régression : charbon noir. */
class GraphColorIT{
    @BeforeAll
    static void boot(){
        HeadlessGame.start();
    }

    @Test
    void everyItemCurveIsReadable(){
        Color bg = LineGraph.BACKGROUND;
        float bgLum = Contrast.luminance(bg.r, bg.g, bg.b);
        for(Item item : content.items()){
            Color c = LineGraph.readable(item.color);
            float ratio = Contrast.ratio(Contrast.luminance(c.r, c.g, c.b), bgLum);
            assertTrue(ratio >= Contrast.MIN_RATIO - 0.01f, item.name + " illisible : contraste " + ratio);
        }
    }
}
