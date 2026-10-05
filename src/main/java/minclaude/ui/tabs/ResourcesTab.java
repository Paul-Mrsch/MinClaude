package minclaude.ui.tabs;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import minclaude.stats.*;
import minclaude.tracking.ResourceTracker;
import minclaude.ui.LineGraph;
import minclaude.ui.ResourceOverlay;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.ui.Styles;

import static mindustry.Vars.content;

/** Onglet Ressources : liste, graphiques du stock et des entrées/sorties, chiffres clés, goulots liés à la ressource. */
public class ResourcesTab extends DashboardTab{
    static final Color IN_COLOR = Pal.heal, OUT_COLOR = Pal.remove;

    private final Table itemList = new Table();
    private final Table details = new Table();
    private final LineGraph stockGraph = new LineGraph().minHeight(200f);
    private final LineGraph flowGraph = new LineGraph().minHeight(140f);
    private Item selected;

    public ResourcesTab(ResourceTracker tracker){
        super("resources", Icon.box, tracker);
    }

    public void select(Item item){
        selected = item;
        refresh();
    }

    @Override
    public void build(Table body){
        body.pane(Styles.smallPane, itemList).width(250f).growY().top();
        body.table(right -> {
            rangeBar(right);
            right.add(stockGraph).grow().pad(4f).row();
            right.table(legend -> {
                legend.add("■ " + text("minclaude.stat.in")).color(IN_COLOR).padRight(16f);
                legend.add("■ " + text("minclaude.stat.out")).color(OUT_COLOR).padRight(16f);
                legend.label(() -> text(tracker.exactFlows() ? "minclaude.stat.exact" : "minclaude.stat.estimated")).color(Color.gray);
            }).left().row();
            right.add(flowGraph).growX().pad(4f).row();
            right.add(details).growX().pad(4f);
        }).grow().top();
    }

    @Override
    public void refresh(){
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
            }, Styles.flatTogglet, () -> select(item)).checked(item == selected).growX().height(44f).row();
        }

        stockGraph.clearSeries();
        flowGraph.clearSeries();
        details.clear();
        if(selected == null){
            details.add(text("minclaude.overlay.empty")).color(Color.lightGray);
            return;
        }

        MetricHistory h = tracker.history();
        stockGraph.addSeries(h.view(ResourceStats.stockKey(selected.name), range), selected.color);
        flowGraph.addSeries(scaled(h.view(ResourceStats.inKey(selected.name), range), 60f), IN_COLOR);
        flowGraph.addSeries(scaled(h.view(ResourceStats.outKey(selected.name), range), 60f), OUT_COLOR);

        ResourceStats s = tracker.stats(selected);
        details.left();
        stat(details, text("minclaude.stat.stock"), Format.amount(s.stock()) + " / " + Format.amount(s.capacity()), Color.white);
        stat(details, text("minclaude.stat.in"), Format.ratePerMinute(s.inPerSec()), IN_COLOR);
        stat(details, text("minclaude.stat.out"), Format.ratePerMinute(-s.outPerSec()), OUT_COLOR);
        stat(details, text("minclaude.stat.net"), Format.ratePerMinute(s.slopePerSec()), ResourceOverlay.rateColor(s.slopePerSec()));
        var lostSeries = h.get(ResourceStats.lostKey(selected.name));
        float lost = lostSeries == null ? 0f : Trend.mean(lostSeries.recent(ResourceStats.DEFAULT_WINDOW));
        if(lost > 0f) stat(details, text("minclaude.stat.lost"), Format.ratePerMinute(-lost), Pal.remove);
        details.row();
        stat(details, text("minclaude.stat.empty"), Format.duration(s.secondsToEmpty()), Pal.remove);
        stat(details, text("minclaude.stat.full"), Format.duration(s.secondsToFull()), Pal.accent);

        // Capacité installée : ce que les usines construites produisent / consomment à plein régime.
        var flow = tracker.base().production().flow(selected.name);
        details.row();
        if(flow != null && flow.installedOut > 0){
            stat(details, text("minclaude.prod.installed"), Format.ratePerMinute(flow.installedOut), IN_COLOR);
            stat(details, text("minclaude.prod.used"), Math.round(flow.utilization() * 100) + "%", flow.utilization() >= 0.9f ? IN_COLOR : Pal.accent);
        }
        if(flow != null && flow.installedIn > 0){
            stat(details, text("minclaude.prod.demand"), Format.ratePerMinute(-flow.installedIn), OUT_COLOR);
        }
        if(flow == null || (flow.installedOut <= 0 && flow.installedIn <= 0)){
            details.add(text("minclaude.prod.none")).color(Color.lightGray).colspan(4).left().padBottom(6f);
        }

        // Objectif de stock fixé par le joueur.
        details.row();
        var goals = tracker.goals();
        Item item = selected;
        if(goals.has(item.name)){
            int goal = goals.get(item.name);
            float eta = Trend.secondsUntil(s.stock(), s.slopePerSec(), goal);
            stat(details, text("minclaude.goal.title"), Format.amount(goal) + " (" + Math.round(goals.progress(item.name, s.stock()) * 100) + "%)", Pal.accent);
            stat(details, text("minclaude.goal.eta"), s.stock() >= goal ? text("minclaude.goal.done") : Format.duration(eta), Pal.accent);
        }
        details.button(text(goals.has(item.name) ? "minclaude.goal.edit" : "minclaude.goal.set"), Icon.starSmall, Styles.flatt, () ->
            mindustry.Vars.ui.showTextInput(text("minclaude.goal.title"), Core.bundle.format("minclaude.goal.prompt", item.localizedName), 7,
                String.valueOf(Math.max(goals.get(item.name), (int)s.capacity() / 2)), true, value -> {
                    goals.set(item.name, arc.util.Strings.parseInt(value, 0));
                    refresh();
                })
        ).height(40f).minWidth(200f).left().padTop(4f);

        // Goulot : combien d'usines sont arrêtées faute de cette ressource.
        int starved = tracker.base().industry().starvedBy(selected.name);
        if(starved > 0){
            details.row();
            details.add(Core.bundle.format("minclaude.industry.starvedby", starved)).color(Pal.remove).colspan(4).left();
        }
    }
}
