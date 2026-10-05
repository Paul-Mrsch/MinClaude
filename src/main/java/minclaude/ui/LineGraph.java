package minclaude.ui;

import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.scene.Element;
import arc.struct.Seq;
import arc.util.Align;
import minclaude.stats.Contrast;
import minclaude.stats.Format;
import minclaude.stats.SeriesView;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;

/**
 * Graphique en courbes : plusieurs séries alignées à droite (la dernière valeur = maintenant),
 * axe Y de 0 au maximum observé, grille légère et étiquettes de valeur et de durée.
 */
public class LineGraph extends Element{
    private static final int GRID_LINES = 4;

    private record Line(SeriesView view, Color color){}

    private final Seq<Line> lines = new Seq<>();
    private float minHeight = 160f;

    public LineGraph minHeight(float height){
        minHeight = height;
        return this;
    }

    public void clearSeries(){
        lines.clear();
    }

    /** La couleur est éclaircie si besoin pour rester lisible sur le fond (ex. charbon, presque noir). */
    public void addSeries(SeriesView view, Color color){
        lines.add(new Line(view, readable(color)));
    }

    /** Fond de référence des graphiques. */
    public static final Color BACKGROUND = Pal.darkestGray;

    public static Color readable(Color c){
        float[] v = Contrast.readableOn(c.r, c.g, c.b, BACKGROUND.r, BACKGROUND.g, BACKGROUND.b);
        return new Color(v[0], v[1], v[2], 1f);
    }

    @Override
    public float getPrefHeight(){
        return minHeight;
    }

    @Override
    public float getPrefWidth(){
        return 400f;
    }

    @Override
    public void draw(){
        validate();
        // Marges intérieures : haut et bas réservés aux étiquettes pour qu'elles ne chevauchent pas les courbes.
        float pad = 4f, labelBand = 18f;
        float gx = x + pad, gy = y + labelBand, gw = width - pad * 2, gh = height - labelBand * 2;

        Draw.color(BACKGROUND, 0.85f * parentAlpha);
        Fill.crect(x, y, width, height);

        float max = 0f, duration = 0f;
        for(Line l : lines){
            max = Math.max(max, l.view.max());
            duration = Math.max(duration, l.view.durationSeconds());
        }
        if(max <= 0f) max = 1f;
        max *= 1.1f;

        Lines.stroke(1f);
        Draw.color(Pal.gray, 0.5f * parentAlpha);
        for(int i = 1; i < GRID_LINES; i++){
            float ly = gy + gh * i / GRID_LINES;
            Lines.line(gx, ly, gx + gw, ly);
        }

        Lines.stroke(2f);
        for(Line l : lines){
            float[] v = l.view.values();
            if(v.length < 2) continue;
            // Une série plus courte que la plage se termine quand même sur le bord droit.
            float span = duration <= 0 ? 1f : duration;
            float startX = gx + gw * (1f - l.view.durationSeconds() / span);
            float dx = gw * l.view.stepSeconds() / span;
            Draw.color(l.color, parentAlpha);
            for(int i = 1; i < v.length; i++){
                Lines.line(startX + dx * (i - 1), gy + gh * v[i - 1] / max, startX + dx * i, gy + gh * v[i] / max);
            }
        }

        Font font = Fonts.outline;
        font.getData().setScale(0.8f);
        font.setColor(Color.lightGray);
        font.draw(Format.amount(max), gx + 2f, y + height - 3f, Align.left);
        font.draw("0", gx + 2f, y + labelBand - 3f, Align.left);
        font.draw("-" + Format.duration(duration), gx + gw - 2f, y + labelBand - 3f, Align.right);
        font.getData().setScale(1f);
        font.setColor(Color.white);
        Draw.reset();
    }
}
