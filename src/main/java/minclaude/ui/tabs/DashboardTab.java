package minclaude.ui.tabs;

import arc.Core;
import arc.graphics.Color;
import arc.scene.style.Drawable;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.layout.Table;
import minclaude.stats.SeriesView;
import minclaude.stats.TimeRange;
import minclaude.tracking.ResourceTracker;
import mindustry.ui.Styles;

/** Un onglet du dashboard. {@link #build} construit la mise en page, {@link #refresh} met à jour les données (chaque seconde). */
public abstract class DashboardTab{
    public final String key;
    public final Drawable icon;
    protected final ResourceTracker tracker;
    protected TimeRange range = TimeRange.TEN_MINUTES;

    protected DashboardTab(String key, Drawable icon, ResourceTracker tracker){
        this.key = key;
        this.icon = icon;
        this.tracker = tracker;
    }

    public String title(){
        return Core.bundle.get("minclaude.tab." + key);
    }

    public abstract void build(Table body);

    public abstract void refresh();

    public void setRange(TimeRange r){
        range = r;
        refresh();
    }

    /** Barre de choix de la plage de temps, commune aux onglets à graphiques. */
    protected void rangeBar(Table t){
        t.table(ranges -> {
            var group = new ButtonGroup<>();
            for(TimeRange r : TimeRange.values()){
                ranges.button(Core.bundle.get("minclaude.range." + r.name().toLowerCase()), Styles.togglet, () -> setRange(r))
                    .group(group).checked(b -> range == r).height(40f).minWidth(90f).pad(2f);
            }
        }).left().row();
    }

    /** Une valeur avec son libellé au-dessus. */
    protected static void stat(Table t, String label, String value, Color color){
        t.table(c -> {
            c.add(label).color(Color.lightGray).left().row();
            c.add(value).color(color).left();
        }).left().padRight(24f).padBottom(6f);
    }

    protected static String text(String key){
        return Core.bundle.get(key);
    }

    /** Les flux sont stockés par seconde ; ils sont affichés par minute, plus parlant. */
    protected static SeriesView scaled(SeriesView v, float factor){
        float[] out = new float[v.values().length];
        for(int i = 0; i < out.length; i++) out[i] = v.values()[i] * factor;
        return new SeriesView(out, v.stepSeconds());
    }
}
