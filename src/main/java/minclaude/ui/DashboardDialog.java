package minclaude.ui;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.layout.Table;
import minclaude.stats.*;
import minclaude.tracking.ResourceTracker;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import static mindustry.Vars.*;

/**
 * Dashboard de gestion. V0 : onglet Ressources (liste, graphiques stock et entrées/sorties, chiffres clés).
 * Les onglets Énergie, Défense et Industries sont affichés mais désactivés jusqu'à la V1.
 */
public class DashboardDialog extends BaseDialog{
    private static final Color IN_COLOR = Pal.heal, OUT_COLOR = Pal.remove;

    private final ResourceTracker tracker;
    private final Table itemList = new Table();
    private final Table details = new Table();
    private final LineGraph stockGraph = new LineGraph().minHeight(200f);
    private final LineGraph flowGraph = new LineGraph().minHeight(140f);
    private float refreshTimer;
    private Item selected;
    private TimeRange range = TimeRange.TEN_MINUTES;

    public DashboardDialog(ResourceTracker tracker){
        super(Core.bundle.get("minclaude.dashboard.title"));
        this.tracker = tracker;
        addCloseButton();
        shown(this::rebuild);
        // Temps réel et non temps de jeu : le dashboard reste à jour quand la partie est en pause.
        update(() -> {
            refreshTimer += Core.graphics.getDeltaTime();
            if(isShown() && refreshTimer >= 1f){
                refreshTimer = 0f;
                refresh();
            }
        });
    }

    private void rebuild(){
        cont.clear();

        cont.table(tabs -> {
            tabs.defaults().height(44f).minWidth(140f).pad(2f);
            tabs.button(Core.bundle.get("minclaude.tab.resources"), Icon.box, Styles.togglet, () -> {}).checked(true);
            for(String tab : new String[]{"power", "defense", "industry"}){
                tabs.button(Core.bundle.get("minclaude.tab." + tab), Styles.togglet, () -> {}).disabled(true)
                    .tooltip(Core.bundle.get("minclaude.tab.soon"));
            }
        }).left().row();

        cont.table(body -> {
            body.pane(Styles.smallPane, itemList).width(250f).growY().top();
            body.table(right -> {
                right.table(ranges -> {
                    var group = new ButtonGroup<>();
                    for(TimeRange r : TimeRange.values()){
                        ranges.button(Core.bundle.get("minclaude.range." + r.name().toLowerCase()), Styles.togglet, () -> {
                            range = r;
                            refresh();
                        }).group(group).checked(r == range).height(40f).minWidth(90f).pad(2f);
                    }
                }).left().row();
                right.add(stockGraph).grow().pad(4f).row();
                right.table(legend -> {
                    legend.add("■ " + Core.bundle.get("minclaude.stat.in")).color(IN_COLOR).padRight(16f);
                    legend.add("■ " + Core.bundle.get("minclaude.stat.out")).color(OUT_COLOR);
                }).left().row();
                right.add(flowGraph).growX().pad(4f).row();
                right.add(details).growX().pad(4f);
            }).grow().top();
        }).grow();

        refresh();
    }

    private void refresh(){
        var items = content.items().select(tracker::isTracked);
        if(selected == null || !tracker.isTracked(selected)) selected = items.isEmpty() ? null : items.first();

        itemList.clear();
        itemList.top();
        for(Item item : items){
            ResourceStats s = tracker.stats(item);
            itemList.button(b -> {
                b.left();
                b.image(item.uiIcon).size(28f).padRight(6f);
                b.add(item.localizedName).left().growX();
                b.add(Format.amount(s.stock())).padLeft(6f).padRight(8f);
            }, Styles.flatTogglet, () -> {
                selected = item;
                refresh();
            }).checked(item == selected).growX().height(44f).row();
        }

        stockGraph.clearSeries();
        flowGraph.clearSeries();
        details.clear();
        if(selected == null){
            details.add(Core.bundle.get("minclaude.overlay.empty")).color(Color.lightGray);
            return;
        }

        MetricHistory h = tracker.history();
        stockGraph.addSeries(h.view(ResourceStats.stockKey(selected.name), range), selected.color);
        flowGraph.addSeries(perMinute(h.view(ResourceStats.inKey(selected.name), range)), IN_COLOR);
        flowGraph.addSeries(perMinute(h.view(ResourceStats.outKey(selected.name), range)), OUT_COLOR);

        ResourceStats s = tracker.stats(selected);
        details.defaults().left().padRight(24f);
        stat(Core.bundle.get("minclaude.stat.stock"), Format.amount(s.stock()) + " / " + Format.amount(s.capacity()), Color.white);
        stat(Core.bundle.get("minclaude.stat.in"), Format.ratePerMinute(s.inPerSec()), IN_COLOR);
        stat(Core.bundle.get("minclaude.stat.out"), Format.ratePerMinute(-s.outPerSec()), OUT_COLOR);
        stat(Core.bundle.get("minclaude.stat.net"), Format.ratePerMinute(s.slopePerSec()), ResourceOverlay.rateColor(s.slopePerSec()));
        details.row();
        stat(Core.bundle.get("minclaude.stat.empty"), Format.duration(s.secondsToEmpty()), Pal.remove);
        stat(Core.bundle.get("minclaude.stat.full"), Format.duration(s.secondsToFull()), Pal.accent);
    }

    /** Pilotage programmatique (autotest en jeu). */
    public void setRange(TimeRange r){
        range = r;
        rebuild();
    }

    public void select(Item item){
        selected = item;
        refresh();
    }

    private void stat(String label, String value, Color color){
        details.table(t -> {
            t.add(label).color(Color.lightGray).left().row();
            t.add(value).color(color).left();
        });
    }

    /** Les entrées/sorties sont stockées par seconde ; on les affiche par minute, plus parlant. */
    private static SeriesView perMinute(SeriesView v){
        float[] out = new float[v.values().length];
        for(int i = 0; i < out.length; i++) out[i] = v.values()[i] * 60f;
        return new SeriesView(out, v.stepSeconds());
    }
}
